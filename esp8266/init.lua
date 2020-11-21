chipid = node.chipid()
print("chipid", chipid)

--таблица каналов и портов
channels = {}
channels[0] = {inp=3, out=0}
channels[1] = {inp=7, out=5}
channels[2] = {inp=4, out=6}
channels[3] = {inp=12, out=8}

if node.flashindex() == nil then
  node.flashreload('lfs.img')
end

--Запуск одного из файлов в зависимости от нажатой кнопки 
print("Call starttimer:stop() to stop execution")
starttimer = tmr.create()
starttimer:alarm(5000, tmr.ALARM_SINGLE,  
	function()
		print("Start _init")
        local fi=node.flashindex 
		pcall(fi and fi'_init')
		
		LFS.start()
    end
)