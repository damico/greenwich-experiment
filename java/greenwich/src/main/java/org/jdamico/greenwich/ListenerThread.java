package org.jdamico.greenwich;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.jdamico.gpsd.client.entities.GpsSkyEntity;
import org.jdamico.gpsd.client.entities.GpsTpvEntity;
import org.jdamico.gpsd.client.entities.Satellite;

import com.google.gson.Gson;

import gapchenko.llttz.Converter;
import gapchenko.llttz.IConverter;
import gapchenko.llttz.stores.TimeZoneListStore;

public class ListenerThread extends Thread {

	public static Double X = null;
	public static Double Y = null;
	public static Double Z = null;
	public String statusChar = "!";

	IConverter iconv = null;

	public void run() {
		iconv = Converter.getInstance(TimeZoneListStore.class);
		Gson gson = new Gson();
		GpsSkyEntity gpsSkyEntity;
		GpsTpvEntity gpsTpvEntity;
		Map<String, String> usedSatsMap = new HashMap<>();
		Map<String, String> fixMap = new HashMap<>();
		Map<String, String> timeMap = new HashMap<>();
		Map<String, String> tzMap = new HashMap<>();
		String tzOffset = "---";
		
		while(TimeSpaceRuntime.shouldListenOutput) {
			try {
				if(TimeSpaceRuntime.outputMessageMap.containsKey("WATCH") && TimeSpaceRuntime.outputMessageMap.containsKey("TPV") && TimeSpaceRuntime.outputMessageMap.containsKey("WATCH") && TimeSpaceRuntime.outputMessageMap.containsKey("DEVICES") && TimeSpaceRuntime.outputMessageMap.containsKey("SKY")) {
					gpsSkyEntity = gson.fromJson(TimeSpaceRuntime.outputMessageMap.get("SKY"), GpsSkyEntity.class);
					String device = gpsSkyEntity.getDevice();

					usedSatsMap.put(device, "00");
					List<Satellite> sats = gpsSkyEntity.getSatellites();
					int sumUsedSats = 0;
					if(sats != null && sats.size() > 0) {
						for (Satellite satellite : sats) {
							if(satellite.getUsed()) sumUsedSats++;
						}
						usedSatsMap.put(device, sats.size() >= 0 && sats.size() < 10 ? "0"+String.valueOf(sats.size()): String.valueOf(sats.size()) );
					}

					gpsTpvEntity = gson.fromJson(TimeSpaceRuntime.outputMessageMap.get("TPV"), GpsTpvEntity.class);
					if(gpsTpvEntity.getTime() != null && gpsTpvEntity.getTime().length() == 24) {
						timeMap.put(gpsTpvEntity.getDevice(), gpsTpvEntity.getTime().split("T")[1].split("\\.")[0]);
					}


					String lastTz = tzMap.get(device);
					if(lastTz == null) tzMap.put(device, tzOffset);
					if(gpsSkyEntity.getGdop() !=null && gpsSkyEntity.getGdop() < TimeSpaceRuntime.DOP_MINIMAL_PRECISION && gpsSkyEntity.getPdop() !=null && gpsSkyEntity.getPdop() < TimeSpaceRuntime.DOP_MINIMAL_PRECISION) {

						fixMap.put(device, "*");


					}
					
					if(gpsSkyEntity.getHdop() !=null && gpsSkyEntity.getHdop() < TimeSpaceRuntime.DOP_MINIMAL_PRECISION && gpsSkyEntity.getVdop() !=null && gpsSkyEntity.getVdop() < TimeSpaceRuntime.DOP_MINIMAL_PRECISION) {	

						X = gpsTpvEntity.getLon();
						Y = gpsTpvEntity.getLat();
						Z = gpsTpvEntity.getAlt();
						fixMap.put(gpsTpvEntity.getDevice(), "!");

						if(X != null && Y!=null) {

							usedSatsMap.put(device, sumUsedSats >= 0 && sumUsedSats < 10 ? "0"+String.valueOf(sumUsedSats): String.valueOf(sumUsedSats) );

							TimeZone tz = iconv.getTimeZone(Y, X);
							int offset = tz.getRawOffset()/3600000;
	
							if(offset>=0 && offset <10) tzOffset = "0"+String.valueOf(offset);
							else if(offset < 0 && offset > -10) tzOffset = "-0"+String.valueOf(offset*-1);
							else if((offset > 0 && offset < 1) || (offset < 0 && offset > -1)) tzOffset = "000";
							
							tzMap.put(device, tzOffset);

						}
						
						if(X != null && Y!=null && Z!=null){
							fixMap.put(device, "|");
						}
					}else {
						System.out.println("DOP without precision.");
						fixMap.put(gpsTpvEntity.getDevice(), "?");
					}
				}else System.out.println("NO GPS CLASSES YET.");

				Set<String> keySet = timeMap.keySet();
				Iterator<String> keySetIter = keySet.iterator();
				while (keySetIter.hasNext()) {
					String key = keySetIter.next(); 
					String sats = "--";
					String tz = tzOffset;
					if(usedSatsMap.get(key) != null) sats = usedSatsMap.get(key);
					if(tzMap.get(key) != null) tz = tzMap.get(key);
					String log = sats+fixMap.get(key)+timeMap.get(key)+"T"+tz;
					if(log.contains("null")) {
						
						System.out.println(log);
						log = "- INVALID DATA -";
						
					}
					if(key.equals("/dev/ttyO4")) log = "A"+log;
					if(key.equals("/dev/ttyO1")) log = "B"+log;
					System.out.println("-------------> "+key+" "+log);
					writeStrToFile(log, "/tmp/"+key.replaceAll("/", "_")+".gps");
				}

				Thread.sleep(500);
			} catch (InterruptedException e) {
				System.err.println("Exception at "+this.getClass().getCanonicalName()+": "+e.getMessage());
				System.exit(1);
			}
		}
	}
	public void writeStrToFile(String str, String fileName) {

		FileWriter fw = null;
		BufferedWriter out = null;
		try {
			fw = new FileWriter(fileName);
			out = new BufferedWriter(fw);
			out.write(str);  
		}
		catch (IOException e)
		{
			e.printStackTrace();

		}
		finally
		{
			if(out != null)
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if(fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}	
	}
}
