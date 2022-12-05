package gapchenko.llttz;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TimeZone;

import gapchenko.llttz.stores.Location;
import gapchenko.llttz.stores.TimeZoneStore;

/**
 * @author artemgapchenko
 * Created on 18.04.14.
 */
public class Converter implements IConverter {
	private TimeZoneStore tzStore;
	private static Converter instance = null;

	private Converter(Class clazz) {
		if (!TimeZoneStore.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("Illegal store provided: " + clazz.getName());
		}
		try {
			tzStore = (TimeZoneStore) clazz.newInstance();
			loadData();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static Converter getInstance(final Class clazz) {
		if (instance == null) instance = new Converter(clazz);
		return instance;
	}

	public Class getStoreClass() {
		return tzStore.getClass();
	}

	@Override
	public TimeZone getTimeZone(final double lat, final double lon) {
		return tzStore.nearestTimeZone(new Location(new double[]{lat, lon}));
	}

	private void loadData() {

		try {
			String[] location;
			File source = new File("dist/timezones.csv");
			if(source != null && source.exists() && source.isFile()) {
				Path path = source.toPath();

				List<String> lst = Files.readAllLines(path);
				
				for (int i = 0; i < lst.size(); i++) {
					location = lst.get(i).split(";");
					tzStore.insert(new Location(Double.valueOf(location[1]), Double.valueOf(location[2]), location[0]));
				}

			}


		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}