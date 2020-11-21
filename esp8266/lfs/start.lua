--Инициализация портов
function initPorts()
	for i = 0, 3 do
		--контакты реле, на выход
		gpio.mode(channels[i].out, gpio.OUTPUT) 
		--выключаем все реле
		gpio.write(channels[i].out, gpio.HIGH)
		--Контакты кнопок на прерывание
		gpio.mode(channels[i].inp, gpio.INPUT, gpio.PULLUP)
	end
end

--Установка статуса порта реле
function setChannelStatus(channel, status)
	--При включении и выключении нагрузки возможны помехи на входных портах, взводим флаг чтоб игнорировать любые сигналы на входящих портах
	lastClick = tmr.now()
	if status == "on" then
		gpio.write(channels[channel].out, gpio.LOW)
	else
		gpio.write(channels[channel].out, gpio.HIGH)
	end
    print("Channel", channel, status)
end

-- Анализ нажатых кнопок и запуск соответствующего файла
function start()
	initPorts()
	wifi.sta.setaplimit(1)
	if (gpio.read(channels[1].inp) == gpio.LOW) then
	    wifi.setmode(wifi.STATION)
		dofile("wps.lua")
	elseif (gpio.read(channels[2].inp) == gpio.LOW) then	
		print("Start web server")
		
		wifi.setmode(wifi.SOFTAP)
		local cfg = {}
		cfg.ssid = "WiFiPower"
		cfg.pwd = "12345678"
		cfg.save = false
		wifi.ap.config(cfg)
		
		dofile("web.lua")
	elseif (gpio.read(channels[3].inp) == gpio.LOW) then
		wifi.setmode(wifi.SOFTAP)
		local cfg = {}
		cfg.ssid = "WiFiPower"
		cfg.pwd = "12345678"
		cfg.save = false
		wifi.ap.config(cfg)
		
		dofile("main.lua")
	else
		wifi.setmode(wifi.STATION)
		wifi.sta.connect()
		dofile("main.lua")
	end
end 

start()