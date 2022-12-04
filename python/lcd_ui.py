#!/usr/bin/python3

import smbus
import time
import os
import psutil

# device parameters
#I2C_ADDR  = 0x70 # I2C device address
I2C_ADDR  = 0x27
#LCD_WIDTH = 20   # Maximum characters per line
LCD_WIDTH = 16

# device constants
LCD_CHR = 1 # Mode - Will Send Data
LCD_CMD = 0 # Mode - Will Send Commands

LCD_LINE_1 = 0x80 # LCD RAM address for the 1st line
LCD_LINE_2 = 0xC0 # LCD RAM address for the 2nd line
LCD_LINE_3 = 0x94 # LCD RAM address for the 3rd line
LCD_LINE_4 = 0xD4 # LCD RAM address for the 4th line

LCD_BACKLIGHT  = 0x08  # On
#LCD_BACKLIGHT = 0x00  # Off

ENABLE = 0b00000100 # Enable bit

# Timing constants
E_PULSE = 0.0005
E_DELAY = 0.0005

#Open I2C interface
bus = smbus.SMBus(2) # Rev C Beaglebone Black uses 2

def lcd_init():
  # Initialise display
  lcd_byte(0x33,LCD_CMD) # 110011 Initialise
  lcd_byte(0x32,LCD_CMD) # 110010 Initialise
  lcd_byte(0x06,LCD_CMD) # 000110 Cursor move direction
  lcd_byte(0x0C,LCD_CMD) # 001100 Display On,Cursor Off, Blink Off 
  lcd_byte(0x28,LCD_CMD) # 101000 Data length, number of lines, font size
  lcd_byte(0x01,LCD_CMD) # 000001 Clear display
  time.sleep(E_DELAY)

def lcd_byte(bits, mode):
  # Send byte to data pins
  # bits = the data
  # mode = 1 for data
  #        0 for command

  bits_high = mode | (bits & 0xF0) | LCD_BACKLIGHT
  bits_low = mode | ((bits<<4) & 0xF0) | LCD_BACKLIGHT

  # High bits
  bus.write_byte(I2C_ADDR, bits_high)
  lcd_toggle_enable(bits_high)

  # Low bits
  bus.write_byte(I2C_ADDR, bits_low)
  lcd_toggle_enable(bits_low)

def lcd_toggle_enable(bits):
  # Toggle enable
  time.sleep(E_DELAY)
  bus.write_byte(I2C_ADDR, (bits | ENABLE))
  time.sleep(E_PULSE)
  bus.write_byte(I2C_ADDR,(bits & ~ENABLE))
  time.sleep(E_DELAY)

def lcd_string(message,line):
  # Send string to display

  message = message.ljust(LCD_WIDTH," ")

  lcd_byte(line, LCD_CMD)

  for i in range(LCD_WIDTH):
    lcd_byte(ord(message[i]),LCD_CHR)

def checkIfProcessRunning(processName):
  for proc in psutil.process_iter():
    try:
      if processName.lower() in proc.name().lower():
        return True
    except (psutil.NoSuchProcess, psutil.AccessDenied, psutil.ZombieProcess):
        pass
  return False


def get_init_proc(l, c):
  sep = " "
  if c % 2 == 0: sep = "-"
  proc = 'PID BAD'
  if checkIfProcessRunning('java'):
    proc = 'PID OK'
  m = 'INIT ('+proc+') '+str(l)
  m = m.replace(' ', sep)
  return m

def handle_exc(c):
  sep = " "
  if c % 2 == 0: sep = "-"
  m = 'UI ERROR'
  m = m.replace(' ', sep)
  lcd_string(m, LCD_LINE_1)
  print(m)

def proc_msg(f, l, c):
  sep = ":"
  if c % 2 == 0: sep = "."
  m = ''
  if os.path.exists(f):
    with open(f) as file:
      lines = file.readlines()
      m = lines[0]
  else: m = get_init_proc(l,c)
  if l == 1: addr = LCD_LINE_1
  if l == 2: addr = LCD_LINE_2
  m = m.replace(':', sep)
  lcd_string(m, addr)
  print(m, l)


def main():
  counter = 0
  fA = '/tmp/_dev_ttyO4.gps'
  fB = '/tmp/_dev_ttyO1.gps'
  lcd_init()

  while True:
    try:
      proc_msg(fA, 1, counter)
      proc_msg(fB, 2, counter)
    except:
      handle_exc(counter)
    time.sleep(1)
    counter = counter +1


if __name__ == '__main__':

  try:
    main()
  except KeyboardInterrupt:
    pass
  finally:
    lcd_byte(0x01, LCD_CMD)

