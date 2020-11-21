cfg = {}

function fromHex(hex) 
	return string.char(tonumber(hex,16)) 
end

-- decode URI
function decodeURI(s)
	local result = s
    if (s) then
        result = string.gsub(s, '%%(%x%x)', fromHex)
    end
    return result
end

function restart()
	print("Restart")
	node.restart()
end

function saveAndRestart()
	print("Save config", cfg.ssid, cfg.pwd)
	
	wifi.setmode(wifi.STATION)
	wifi.sta.config(cfg)
	tmr.create():alarm(3000, tmr.ALARM_SINGLE, restart)
end

function onReceive(client, request) 
	print(request)

	local url_file = string.match(request, "[^/]*\/([^ ?]*)[ ?]", 1)
	print("HTTP request url_file:", url_file, "Heap", node.heap())
	
	local uri = string.match(request, "GET [^?]*\?([^ ]*)[ ]", 1)
	print("HTTP request uri:", url, "Heap", node.heap())
 
	-- parse GET parameters
	local params={}
	if uri then
		for key, value in string.gmatch(uri, "([^=&]*)=([^&]*)") do
			params[key]=value
		end
	end
	
	-- parse POST parameters
	local post = string.match(request,"\n([^\n]*)$",1)
	if post then
		post = post:gsub("+", " ")
		for key, value in string.gmatch(post, "([^=&]*)=([^&]*)") do
			params[key] = decodeURI(value)
		end
	end
	
	for k,v in pairs(params) do print("Param", k,v) end
	
	if params["ssid"] then
		cfg.ssid = params["ssid"]
		cfg.pwd = params["password"]
		cfg.save = true		
		tmr.create():alarm(3000, tmr.ALARM_SINGLE, saveAndRestart)		
		url_file = "restart.html"
	end
	
	if url_file == '' then
		url_file = "index.html"
	end 
	
	local filecontent = "HTTP/1.0 200 OK\r\nServer: NodeMCU\r\nContent-Type: text/html\r\n\r\n";
	-- read file:
	print ("Open file ", url_file)
	if file.open(url_file, "r") then
		print ("Read file ", url_file)
	
		local buf = ""
		repeat
			filecontent = filecontent .. buf
			buf = file.read()
		until buf == nil

		file.close()
	end  	
	client:send(filecontent)
end
		
		
function onSent(socket) 
	print ('close socket')
	socket:close() 
	collectgarbage();
end		

function startWebServer()
	-- Запуск TCP сервера 30 сек таймаут неактивных клиентов
	local sv = net.createServer(net.TCP, 30)
	-- Сервер слушает на порту 80
	sv:listen(80, function(socket)
		socket:on("receive", onReceive)
		socket:on("sent", onSent)
	end)
end

startWebServer()