# WiFiAquariumControl
Управление 4 розетками через WIFI с помощью мобильного приложения на Android. В качестве контроллера используется ESP8266.

## Прошивка
Подключаем USB-UART адаптер к плате WIFI пилота (Контакты GND -> GND; Tx -> Rx; Rx -> Tx)
Подключаем USB-UART адаптер к компьютеру под управлением Windows
В диспечере устройств Windows смотрим какой порт у устройства USB-SERIAL CH340, Например отображается COM4, Запоминаем его.
Запускаем ESP8266Flasher, Устанавливаем порт COM4, На вкладке конфиг в верхнем поле выбираем прошивку nodemcu-master-13-modules-2020-02-25-13-26-06-integer.bin
Нажимаем кнопку 1 (Самая ближняя к ESP модулю) на плате WIFI пилота и удерживая ее подключаем питание 5В к плате.
В окне ESP8266Flasher на вкладке Operation нажимаем кнопку Flush, Должны заполнится поля AP MAC и STA MAC и начать заполнятся градусник прогресс бара
Дождатся завершения процесса и отключить питание от платы.

## Установка скриптов
Скрипты прежде чем закачать на устройство надо откомпилировать и собрать в lfs архив.
Сборка lfs архива на windows. Устанавливаем нужное ПО.
установить linux for windows
установить дистрибутив ubuntu 18.04
запустить Ubuntu
Установить Toolchain:
sudo apt-get update
sudo apt-get install build-essential
Скачать исходники компилятора Lua https://github.com/nodemcu/nodemcu-firmware/archive/master.zip и рархивировать их
отредактировать файл nano app/include/user_config.h расскоментировать строчку:
#define LUA_NUMBER_INTEGRAL
собрать компилятор lua:
cd app/lua/luac_cross
make

компиляция lua скриптов в папке командой
cd /mnt/c/Users/Mikhail/Documents/projects/avr/WiFiAquariumControl/esp8266
~/nodemcu/nodemcu-firmware-master/luac.cross.int -o lfs.img -f lfs/*.lua
или онлайн сервисом https://blog.ellisons.org.uk/article/nodemcu/a-lua-cross-compile-web-service/
Загрузить LSF на устройство с помощью программы ESPlorer и загрузить в память командой:
=node.flashreload('lfs.img')

## Настройка подключения к WIFI:
Включить устройство и через 1 секунду нажать кнопку 3, и держать 5 сек. Устройство перейдет в режим конфигурации.
Появится новая WiFi сеть с именем WiFiPower пароль 12345678, необходимо подключится к ней с телефона.
Зайти по адресу http://192.168.4.1/
Отобразится страница конфигурации wifi пилота.
В поле SSID впишите имя wifi сети в котоорой будет работать пилот, в поле Password пароль к этой сети. Нажмите save and restart. 
Устройство перезагрузится и попытается подключится к настроенной сети. 

В программе на телефоне нажмите меню выберите редактор устройств. Далее опять меню и пункт добавить. 
Произойдет поиск устройства и в случае если найдено отобразится список в котором будет устройство. Выберите его из списка. Устройство добавится.
Вернитесь на предыдущий экран, там отобразится пиктограмма с устройством. Нажмите его попадете в окно к=управления конкретным устройством.

Устройство может работать без точки доступа. Удобно для показа.
Для этого посде включения питания через 1 секунду нажмите кнопку 4 и держите 5 секунд. Устройство запустится в режиме точки доступа. 
Далее нужно подключится с телефона к этой точке доступа WiFiPower пароль 12345678.
Далее как и в случае с предыдущим варианом использования надо поискать и добавить устройство.


Подключение платы с контроллером к плате с реле:
0 -> IN1
1 -> IN2 
2 -> IN3
3 -> IN4
4 -> VCC
5 -> JD-VCC
6 -> GND
