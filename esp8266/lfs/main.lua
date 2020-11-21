ds1307 = LFS.ds1307()

--Файл настроек
settingsFileName = "settings.json"
settingsFileNameOld = settingsFileName .. ".old"
--Текущие настройки
settings = {}
--Служебная переменная для избавления от дребезга контактов
lastClick = 0
--Флаг наличия обновления
hasUpdate = false

--подписываемся на прерывания от кнопок
function onClick(channel)
    gpio.trig(channels[channel].inp, "down", function(level, when, eventCount) 
		--защита от дребезга контактов
        local now = tmr.now()
        local pause = 200000
        if (lastClick < now and (now - lastClick) > pause) or lastClick > now then
            lastClick = now
			print("Press button", channel)
            if gpio.read(channels[channel].out) == gpio.HIGH then
                setChannelStatus(channel, "on")
            else
                setChannelStatus(channel, "off")
            end
            startLongClick = tmr.now();

            if (gpio.read(channels[0].inp) == gpio.LOW and gpio.read(channels[3].inp) == gpio.LOW) then
                startWiFiConfig()
            end
        end

    end)
end

function getDefaultSettings()
	local result = {}
	result.lighting = {}
	result.lighting.enable = false
	return result
end

--Загрузка конфигурации
function loadSettings()
    if file.exists(settingsFileName) then
        if file.open(settingsFileName, "r") then
            print ("Open settings file")
            local buf = ""
            local res = ""
            repeat
                res = res .. buf
                buf = file.read()
            until buf == nil
            file.close()
            print("Settings=",res)
            settings = sjson.decode(res)
        else
            print("Error open settings file")
            settings = getDefaultSettings()
        end
    else
		print ("Settings file not found")
        settings = getDefaultSettings()
    end 
end

--Сохранение конфигурации
function saveSettings()
	--Старую конфигурацию сначала удаляем а потом переименовываем текущую в old
	file.remove(settingsFileNameOld)
	file.rename(settingsFileName, settingsFileNameOld)
	
	local saveComplete = false
	local err = ""
	--Счетчик попыток сохранения
	local saveCount = 0
	repeat
		--Сохраняем новую конфигурацию
		file.open(settingsFileName, "w+")
		file.write(sjson.encode(settings))
		file.close()

		saveCount = saveCount + 1
		
		--Проверяем корректно ли записалась конфигурация, пытаемся ее зачитать
		saveComplete, err = pcall(loadSettings)
		if not saveComplete then
			print("Save config error", err)
		else
			print("Config saved")
		end
	until saveComplete or saveCount > 10
	
	--Если за отведенное количество попыток сохранение не удалось то возвращаем старый файл настроек
	if not saveComplete then
		file.remove(settingsFileName)
		file.rename(settingsFileNameOld, settingsFileName)
	end
end

--Получение статуса
function getStatus()
	local res = {}
	res.channels = {}
	
	for i = 0, 3 do
		if gpio.read(channels[i].out) == gpio.HIGH then
			res.channels[i] = "off"
		else
			res.channels[i] = "on"
		end			
	end
	res.time = ds1307.getTime()
	res.lighting = 1024 - adc.read(0)
	
	if wifi.getmode() == wifi.SOFTAP then
		res.ip = wifi.ap.getip()
	else
		res.ip = wifi.sta.getip()
	end
    
	local chip_id, flash_id, flash_size, flash_mode, flash_speed = node.info("hw")
    res.id = chipid
	res.hasUpdate = hasUpdate
	local result = sjson.encode(res)
	print ("Heap", node.heap())
	return result
end

--Обработка TCP команды
function executeCommand(arg)
    for k,v in pairs(arg) do print(k,v) end

    local result = "OK"
    if arg.command == "on" then
        print ("ON channel", arg.channel)
		setChannelStatus(arg.channel, arg.command)
		result = getStatus()
    elseif arg.command == "off" then
        print ("OFF channel", arg.channel)		
		setChannelStatus(arg.channel, arg.command)
		result = getStatus()
    elseif arg.command == "settings-get" then
        print ("Get settings")
        result = sjson.encode(settings)
    elseif arg.command == "settings-set" then
        print ("Set settings")
        settings = arg.settings
        saveSettings()
		result = getStatus()
    elseif arg.command == "get-status" then
        print ("Get status")
        result = getStatus()
    elseif arg.command == "set-time" then
        print ("Set time", arg.time.hour, arg.time.min, arg.time.sec)
        ds1307.setTime(arg.time)
		result = getStatus()
    elseif arg.command == "sync-time" then
        print ("Sync time")
        ds1307.sntpSync()
		result = getStatus()
    elseif arg.command == "update-firmware" then
        print ("Update firmware")
		dofile("update-firmware.lua")
		collectgarbage()
        result = getStatus()
    end
    
    return result
end

--Функция срабатывает раз в день в 00:00
function onDayTimer()
	--Ежедневная синхронизация времени
	ds1307.sntpSync()
end

--Обработчик минутного таймера (точнее 30 секундного)
function onMinTimer()
    local time = ds1307.getTime()    
    print ("Current time", time["date"], time["month"], time["year"], time["day"], time["hour"], time["min"], time["sec"])

    --Запуск ежедневных операций
    if time.todayMin == 0 then
		onDayTimer()        
    end

    --Проверка на срабатывание таймера
    if settings ~= nil and settings.schedules ~= nil then
        for i, schedule in ipairs(settings.schedules) do 
            if schedule.time == time.todayMin and bit.isset(schedule.day, time.day - 1) then    
				setChannelStatus(schedule.channel, schedule.command)
                print("Schedule channel", schedule.channel, schedule.command)
            end
        end
    end
    
    print ("Heap", node.heap())    
end 

--Получение стартового состояния канала
function getStartChannelStatus(channel)
	local time = ds1307.getTime()	
	local maxValue = nil
	local maxValueCommand = nil
	local nearLowValue = nil
	local nearLowValueCommand = nil
	--Цикл по конфигурации рассписания
	if settings ~= nil and settings.schedules ~= nil then
		for i, schedule in ipairs(settings.schedules) do
            --print("schedule", i, schedule)
			--Отбираем для нашего канала
			if schedule.channel == channel then
				--Ищем максимальный
				if maxValue == nil or maxValue < schedule.time then 
					maxValue = schedule.time
					maxValueCommand = schedule.command
				end
				
				--Ищем ближайший меньший
				if schedule.time < time.todayMin then
					if nearLowValue == nil or schedule.time > nearLowValue then 
						nearLowValue = schedule.time
						nearLowValueCommand = schedule.command
					end
				end
			end
		end
		--Устанавливаем порты канала сначала в ближайший меньший, а если такой не найден то в максимальный
		if nearLowValueCommand ~= nil then
			return nearLowValueCommand
		elseif maxValueCommand ~= nil then
			return maxValueCommand
		else
			return "off"
		end
		
    else
        return "off" 
	end
end

function getChannelStatus(channel)
	if gpio.read(channels[channel].out) == gpio.LOW then
		return "on"
	else
		return "off"
	end
end

--Обработчик секундного таймера
function onSecTimer() 
    --Проверка на освещение
    if settings ~= nil and settings.lighting ~= nil and settings.lighting.enable ~= nil and settings.lighting.enable then
        local lighting = 1024 - adc.read(0) 
        if lighting < settings.lighting.on then
			--Датчик освещенности дает сигнал на включение канала, проверяем разрешает ли рассписание
			local startStatus = getStartChannelStatus(settings.lighting.channel)
			if startStatus == "on" then
				if (getChannelStatus(settings.lighting.channel) == "off") then
					setChannelStatus(settings.lighting.channel, "on")
					print("Lighting senser channel", settings.lighting.channel, "ON")
				end
			end
        elseif lighting > settings.lighting.off then
			--Датчик освещенности дает команду на отключение канала
			if (getChannelStatus(settings.lighting.channel) == "on") then
				setChannelStatus(settings.lighting.channel, "off")
				print("Lighting senser channel", settings.lighting.channel, "OFF")
			end
        end
    end
end

--Метод инициализации
function init()
	--tmr.stop(0)

	local ip = wifi.sta.getip()
	print("Local IP", ip)	
	
	ds1307.init(1, 2, 3, "europe.pool.ntp.org")
	ds1307.sntpSync()	
	
	--Инициализация портов
	for i = 0, 3 do
		--контакты реле, на выход
		gpio.mode(channels[i].out, gpio.OUTPUT) 
		--выключаем все реле
		setChannelStatus(i, "off")
		--Контакты кнопок на прерывание
		gpio.mode(channels[i].inp, gpio.INT, gpio.PULLUP) 
		--Подписывемся на прерывания
		onClick(i)
	end
	
	--загрузка конфигурации
	local saveComplete, err = pcall(loadSettings)
	if not saveComplete then
		if file.exists(settingsFileNameOld) then
			print("Error load settings. Load old settings", err)
			file.remove(settingsFileName)
			file.rename(settingsFileNameOld, settingsFileName)
			loadSettings()
		else
			print("Error load settings. Load default settings", err)
			settings = getDefaultSettings()
		end
	end

    --Инициализация начального состояния каналов
    for i = 0, 3 do
        local startStatus = getStartChannelStatus(i)
        setChannelStatus(i, startStatus)
    end
    
	-- Запуск TCP сервера 30 сек таймаут неактивных клиентов
	local sv = net.createServer(net.TCP, 30)
	-- Сервер слушает на порту 7456
	sv:listen(7456, function(c)
		local result = nil
		c:on("receive", function(c, pl) 
			print(pl)
			result = executeCommand(sjson.decode(pl))
            c:send(result)
		end)
		c:on("sent",function(conn) conn:close() end)
	end)

	--Запуск UDP сервера для обнаружения устройства
	local udp = net.createUDPSocket()
	-- Сервер слушает на порту 7456
	udp:listen(7456)
	udp:on("receive", function(c, pl, port, ip) 
		print("UDP receive", pl)
		local result = {}

		if wifi.getmode() == wifi.SOFTAP then
			result.ip = wifi.ap.getip()
		else
			result.ip = wifi.sta.getip()
		end

		local chip_id, flash_id, flash_size, flash_mode, flash_speed = node.info("hw")
		result.id = chipid
		print(sjson.encode(result))
        c:send(port, ip, sjson.encode(result))
	end)
	
	--Проверяем расписание раз в 30 сек
	tmr.create():alarm(30000, tmr.ALARM_AUTO, onMinTimer)	
	
	--Проверка освещенности, раз в 1 секунд
	tmr.create():alarm(1000, tmr.ALARM_AUTO, onSecTimer)
	
end

--Выполняем инициализацию
init()