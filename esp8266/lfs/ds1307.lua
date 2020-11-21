local ds1307 = {}

ds1307.dev_addr = 0x68
ds1307.timezone = 0
ds1307.ntp_server = 0

function ds1307.read_reg(reg_addr)
    i2c.start(0)
    i2c.address(0, ds1307.dev_addr, i2c.TRANSMITTER)
    i2c.write(0, reg_addr)
    i2c.stop(0)
    i2c.start(0)
    i2c.address(0, ds1307.dev_addr, i2c.RECEIVER)
    local res = i2c.read(0, 1)
    i2c.stop(0)
    return res
end

function ds1307.init(pinSDA, pinSCL, timezone, ntp_server)
	ds1307.timezone = timezone
	ds1307.ntp_server = ntp_server
	
    i2c.setup(0, pinSDA, pinSCL,  i2c.SLOW)
    local reg = ds1307.read_reg(0x00)
    if bit.isset(string.byte(reg), 7) then
        i2c.start(0)
        i2c.address(0, ds1307.dev_addr, i2c.TRANSMITTER)
        i2c.write(0, 0x00, 0x00, 0x00, 0x00)
        i2c.stop(0)        
    end
end

--TODO надо реализовать получение даты мес¤ца и года а так же скорректировать условие, при котором надо синхронизировать врем¤
function ds1307.sntpSync()
    sntp.sync(ds1307.ntp_server, function(sntpsec, usec, server)
			--текущее врем¤
            local currentSec = ds1307.getTime()["todaySec"]

            print('sntp responce', sntpsec, usec, server)
            --”читываем таймзону
            local sec = sntpsec + (ds1307.timezone * 3600)
            local weekSec = sec % 604800
            local sec = (sec % 86400)
            print (currentSec, sec)

            if currentSec == sec then
                print("Time is correct/ Not need sync")
            else
                local time = {}
                time.sec = sec % 60
                time.min = ((sec % 3600) - time.sec) / 60 
                time.hour = (((sec % 86400) - (sec % 3600)) / 3600 )
				time.day = (((((weekSec % 604800) - (weekSec % 86400)) / 86400 ) +3) % 7) + 1
				--TODO число мес¤ц год
                time.date = 10
                time.month = 6
                time.year = 16
              
                print("sntp time", sec,  time["date"], time["month"], time["year"], time["day"], time["hour"], time["min"], time["sec"])
                print("local time", currentSec)
                print("sync time", sec - currentSec)
                
                ds1307.setTime(time)
            end
            
        end, function(err, message)
            print('failed sync time!', err, message)
    end)    
end

function ds1307.getTime()
    i2c.start(0)
    i2c.address(0, ds1307.dev_addr, i2c.TRANSMITTER)
    i2c.write(0, 0x00)
    i2c.stop(0)
    i2c.start(0)
    i2c.address(0, ds1307.dev_addr, i2c.RECEIVER)
    local draftTime = i2c.read(0, 7)
    i2c.stop(0)
	
    local sec = string.byte(draftTime, 1)
    local min = string.byte(draftTime, 2)
    local hour = string.byte(draftTime, 3)
	local day = string.byte(draftTime, 4)
	local date = string.byte(draftTime, 5)
	local month = string.byte(draftTime, 6)
	local year = string.byte(draftTime, 7)
	
    local result = {}
    result["sec"] = bit.band(sec, 0x0F) + 10 * bit.rshift(bit.band(sec, 0xF0), 4)
    result["min"] = bit.band(min, 0x0F) + 10 * bit.rshift(bit.band(min, 0xF0), 4)
    result["hour"] = bit.band(hour, 0x0F) + 10 * bit.rshift(bit.band(hour, 0xF0), 4)
	result["day"] = bit.band(day, 0x0F)
	result["date"] = bit.band(date, 0x0F) + 10 * bit.rshift(bit.band(date, 0xF0), 4)
	result["month"] = bit.band(month, 0x0F) + 10 * bit.rshift(bit.band(month, 0xF0), 4)
	result["year"] = bit.band(year, 0x0F) + 10 * bit.rshift(bit.band(year, 0xF0), 4)
	
	result["todayMin"] = result.hour * 60 + result.min
	result["todaySec"] = result.hour * 3600 + result.min * 60 + result.sec
	
    return result
end

function ds1307.setTime(time)
    local sec = bit.bor(bit.lshift(time["sec"] / 10, 4), time["sec"] % 10)
    local min = bit.bor(bit.lshift(time["min"] / 10, 4), time["min"] % 10)
    local hour = bit.bor(bit.lshift(time["hour"] / 10, 4), time["hour"] % 10)
	local day = bit.bor(bit.lshift(time["day"] / 10, 4), time["day"] % 10)
	local date = bit.bor(bit.lshift(time["date"] / 10, 4), time["date"] % 10)
	local month = bit.bor(bit.lshift(time["month"] / 10, 4), time["month"] % 10)
	local year = bit.bor(bit.lshift(time["year"] / 10, 4), time["year"] % 10)

    i2c.start(0)
    i2c.address(0, ds1307.dev_addr, i2c.TRANSMITTER)
    i2c.write(0, 0x00, sec, min, hour, day, date, month, year)
    i2c.stop(0)
end

return ds1307
