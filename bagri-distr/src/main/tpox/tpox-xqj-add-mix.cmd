@echo off
@
setlocal

:config

set TPoX_HOME=C:\Work\Bagri\TPoX
set app_home=.

rem specify the JVM heap size
rem set memory=1024m
set memory=256m

rem set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_65

:start
if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% 

rem set java_opts=%java_opts% -Dtangosol.coherence.proxy.address=localhost
rem set java_opts=%java_opts% -Dtangosol.coherence.proxy.port=21000

set java_opts=%java_opts% -Dlogback.configurationFile=hz-client-logging.xml -Dlog.name=tpox-client
set java_opts=%java_opts% -Dhazelcast.logging.type=slf4j -Dhz.log.level=warn -Dxdm.log.level=info
set java_opts=%java_opts% -Dxdm.spring.context=spring/tpox-client-context.xml

rem set java_opts=%java_opts% -Dxdm.schema.members=192.168.1.100:10500
set java_opts=%java_opts% -Dxdm.schema.members=localhost:10500
set java_opts=%java_opts% -Dxdm.schema.name=TPoX2
set java_opts=%java_opts% -Dxdm.schema.password=TPoX2
set java_opts=%java_opts% -Dxdm.client.submitTo=any
rem possible values are: member, owner, any

rem The number of securities is fixed to 20,833.  Therefore, either 
rem "-u 83 -tr 251"  or "-u 251 -tr 83" can be use to insert the 20833
rem security documents  (because 83 * 251 = 20833). 4166*5 = 3472*6 = 2976*7 = 2604*8 = 2314*9

rem insert securities to the cache
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/insSecurity-xqj.xml -tr 2604 -u 8 

rem insert customers to the cache
rem "%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/insCustacc-xqj.xml -tr 5000 -u 2

rem insert orders to the cache
rem "%java_exec%" -server %java_opts% -cp "%app_home%\target\*;%app_home%\target\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/insOrder-xqj.xml -tr 5000 -u 4

goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xqj-add-mix.cmd
goto exit

:exit
endlocal
@echo on