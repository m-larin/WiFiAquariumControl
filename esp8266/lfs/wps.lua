--tmr.stop(0)

-- Показываем ошибку помигиванием через один светодиод, а потом оставляем гореть светодиод с номером ошибки
function failSignal(err)
    for i = 0, 3 do
		setChannelStatus(0, getStatusFromNum(i % 2))
		setChannelStatus(1, getStatusFromNum((i + 1) % 2))
		setChannelStatus(2, getStatusFromNum(i % 2))
		setChannelStatus(3, getStatusFromNum((i + 1) % 2))
		tmr.delay(200000)
    end
	setAllChannelStatus("off")
	
	setChannelStatus(0, getStatusFromBoolean(bit.isset(err, 0)))
    setChannelStatus(1, getStatusFromBoolean(bit.isset(err, 1)))
    setChannelStatus(2, getStatusFromBoolean(bit.isset(err, 2)))
    setChannelStatus(3, getStatusFromBoolean(bit.isset(err, 3)))
	tmr.delay(3000000)
	setAllChannelStatus("off")
end

function getStatusFromNum(num)
    if (num == 0) then
        return "off"
    else
        return "on"
    end
end

function getStatusFromBoolean(boolean)
    if (boolean) then
        return getStatusFromNum(1)
    else
        return getStatusFromNum(0)
    end 
end


--Установка всех каналов в одинаковый статус
function setAllChannelStatus(status)
    for i = 0, 3 do
        setChannelStatus(i, status)
    end
end

-- Показываем что все ОК
function okSignal()
    setAllChannelStatus("off")
    for i = 0, 3 do
		setAllChannelStatus("off")
		setChannelStatus(i, "on")
		tmr.delay(200000)
    end
    setAllChannelStatus("off")
end

-- Запуск конфигурации WIFI с помощью wps
function startWiFiConfig()
    print("Start WIFI config")
	
	--Сигнализируем что началась настройка
    for i = 0, 3 do
		setAllChannelStatus("on")
		tmr.delay(500000)
		setAllChannelStatus("off")
		tmr.delay(500000)
    end

    wifi.sta.clearconfig()
    wps.disable()
    wps.enable()
    wps.start(function(status)
        if status == wps.SUCCESS then
            wps.disable()
            wifi.sta.connect()
            print("WPS: Success, connecting to AP.")
			okSignal()
        elseif status == wps.FAILED then
            print("WPS: Failed")
            failSignal(1)
        elseif status == wps.TIMEOUT then
            print("WPS: Timeout")
            failSignal(2)
        elseif status == wps.WEP then
            print("WPS: WEP not supported")
            failSignal(3)
        elseif status == wps.SCAN_ERR then
            print("WPS: AP not found")
            failSignal(4)
        else
            print(status)
            failSignal(5)
        end
        wps.disable()
		node.restart()
    end)    
end

startWiFiConfig()