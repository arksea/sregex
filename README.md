sregex
======

A java regex lib, It's non-backtracking and for data streams

#### 一、简介

   流数据的正则表达式库是一个在数据流中进行正则匹配和搜索的工具，支持多模式匹配，通常运用在自动人机交互、网络通讯、大文本搜索等需要对流数据进行处理的应用中，如交换机自动操作程序、终端的命令自动执行。
    
   正则表达式是一种强大的工具，当我们进行文本信息的分析和处理时通常都会用到，C++，JAVA，C#等语言都拥有各自的正则表达式库，而有些语言如Perl甚至内嵌了模式匹配。但是当我们在处理流数据时，上述的正则表达式使用起来并不顺手。因为通常的正则表达式用于搜索一个已知的文本数据，而流数据通常具有不可预知的特性，如：具体内容、长度等。另外在处理大文本时，流式模式匹配也能大大简化程序，如不必将所有文本都读入能内存、不用考虑一次读取多少文本，本次读取的文本是否正好跨越可能匹配的部分文本段，导致必须重复搜索之前的已搜索过的能容等。

#### 二、应用实例

##### 1. 例一
假设在一个大文本中搜索以下样式的数据

```
Albert
   …
   多行的简介,比如特长、喜好等
   …
   student
   173
   16
   62
```

对于传统的方法，因为是大文本，因此不能一次将所有文本读入内存进行搜索，那么在读入部分数据时很可能会碰到读入了部分待匹配文本的情况，处理起来较麻烦；特别是如果文本中存在类似以上样式的数据时，比如：

```
Point
  16
  33
Mike
   …
   多行的简介,比如特长、喜好等
   …
  teacher
  181
  37
  65
```

传统方法的程序有些繁琐，这里就不例举了，有兴趣的读者可以尝试写写，下面我们看看流式模式匹配是如何实现的：
```
FileInputStream in=new FileInputStream(“filename.txt”);
Matcher ma = new Matcher(in);
while(ma.find(“^\\w+\n(\\s+.*\n)*\\s+student\n(\\s+(\\d+)\n){3}”)==0)
{
   arkco.log.Message(ma.getMatched()[0][0]);
}
```

##### 2. 例二
现有一服务将向制式A的交换机提交命令，以新增用户的来电显示业务(号码：8881234)。
命令为
```
ADD SVC:DN=8881234,RV=TRUE;
```
如果执行成功交换机返回报告：
```
REPORT ADD SVC NUM=8881234
...内容省略...
COMMAND END 2005-07-02 13:01:12
```
命令错误返回报告：
```
REPORT ADD SVC NUM=8881234
... 内容省略...
COMMAND ERROR 2005-07-02 13:01:12
```
命令执行失败返回报告：
```
REPORT ADD SVC NUM=8881234
... 内容省略...
COMMAND REJECTION 2005-07-02 13:01:12
```
报告提取超时返回报告：
```
REPORT ADD SVC NUM=8881234
... 内容省略...
REPORT TIMEOUT 2005-07-02 13:01:12
```

还有其它可能的返回报告，但为了简化例子，省略了其它的可能性(以及之前的登录与局向选择交互)，需要注意的是报告的最后一行没有换行符。

1）问题分析
交换机A的报告使用类似COMMAND <STATE> <TIME>的文本来标记报告结束，我们只要在发送命令后，分析接收到的报告，当遇到这些标记后停止接收并进行相应的业务处理。

2）传统的方法
如果报告结束的标记行有换行符，我们可以每次读取一行，然后用传统的正则表达式库判断本行是否匹配模式“COMMAND (\w+) \d{4}-\d\d-\d\d \d\d:\d\d:\d\d”,并根据$1的值作相应处理即可。（程序很简单这里就不列出了）
   
但事实是标记行没有换行符，照搬前述方法显然将在试图读取最后一行时造成服务阻塞；如果在读行时加上超时，程序实时性降低，还可能在交换机忙时误判；如果一个一个字符的判断，则程序的复杂度大大增加，在接入多种交换机制式时，多样的报告格式使接入工作复杂而繁重。

即便最后一行有换行符，面对标记跨越多行的情况我们将不得不一次又一次的重复匹配，效率大大降低，这还不包括其它更复杂的情况，如由于交换机忙造成的间歇性报告等时序问题。

3）使用流式模式匹配
流式模式匹配就是为了解决流式数据的分析处理的复杂性而开发的。使用流式模式匹配，程序自然、明快、并且呈现出一致性、模板化的特点，可以轻松应对各种制式五花八门的报告格式。程序如下（省略了错误处理）

```
Matcher ma = new Matcher(inputStream,buffer);
ma.setThrowTimeoutException(true);
String timeRegex=
"(\\d{4}-\\d\\d-\\d\\d\\s+\\d\\d:\\d\\d:\\d\\d)";
ma.add("COMMAND\\s+END\\s+"+timeRegex);
ma.add("COMMAND\\s+REJECTION\\s+"+timeRegex);
ma.add("COMMAND\\s+ERROR\\s+"+timeRegex);
send(“ADD SVC:DN=8881234,RV=TRUE;”);
switch (ma.find(timeout))
{
    case 0:
        arkco.log.Message(“执行网元命令成功”);
        break;
  case 1:
        throw new CommandFalse_E("执行网元命令失败");
    case 2:
        throw new CommandFalse_E("错误的命令");
    default:
        arkco.log.Assert(false);
        break;
 }
```

##### 3. 例三
在linux下执行telnet命令登陆到另一台主机，接着执行ls命令，提取命令显示的结果。
```
        Process process = null;
        InputStream in = null;
        try
       {   process=Runtime.getRuntime().exec("telnet 192.168.5.20");
            in = process.getInputStream();
            OutputStream out=process.getOutputStream();
            Matcher mat = new Matcher(in);
		    if(mat.find(“ogin:”)!=0) return;
            out.write("arksea\n".getBytes());
            out.flush();
            if(mat.find(“assword:”)!=0) return;
            out.write("123456\n".getBytes());
            out.flush();
            if(mat.find(“/home/arksea>”)!=0) return;
            out.write("ls\n".getBytes());
            out.flush();
            mat.add("/home/arksea>");
String regex = "[\\-\\w]+\\s+\\d+\\s+\\w+\\s+\\w+\\s+\\d+\\s+(\\w+\\s+\\d+\\s+\\d+:\\d+)\\s+(\\S+.log)\\s*\n";
            mat.add(regex);
            while(mat.find()==1)
            {   Submatched[][] g = mat.getLastMatchedResult();
                …
                提取数据
               …
           }
            out.write("quit\n".getBytes());
            out.flush();
            out.write("exit\n".getBytes());
            out.flush();
            out.close();
        }
        catch(Exception ex)
        {   throw new Exception("提取日志信息失败",ex);
        }
        finally
        {   …
      }
```

#### 三、使用基础
##### 1. 安装
将arkj-sregex.jar拷贝到LIB目录，并设置到CLASSPATH中即可，库基于JDK1.5开发，编译成兼容JDK1.4。

##### 2. 常用类
流式模式匹配常用的类有：
```
arkco.sregex.Matcher
arkco.sregex.Submatched
arkco.io.MatchInputStream
arkco.io.IMatchedEvent
```

##### 3. 模式简介 
3.1. 非贪婪匹配 

   流式模式匹配基本兼容Perl，但由于流式数据不能预知数据的特点，流式模式匹配中的量词　? * + {m,n}　为非贪婪匹配，即分别相当于Perl的　??  *?  +?  {m,n}? 　。 请看以下例子：
```
   对于数据流： abc_abc_abc
   
   \w+_　将匹配　abc_ 而不是　abc_abc_ 就相当于Perl的 \w+?_
   
   对于数据流： name=John;height=173;weight=63;age=27;
   
   height=\d+  将匹配　height=173 而不是　height=1,因为\d+是最后一个模式，没有结束标记，所以它是以非数字作为结束判断。
```

3.2. 可展开的提取
arkco.sregex的提取类似于boost.regex是可展开的，这在很多情境下相当方便。

3.3. 模式列表

1）字符

```
\d　――　数字字符(0-9)
\D　――　非数字字符
\n　――　换行符
\r　――　回车符
\t　――　制表符
\s　――　空白字符(空格、\r、\n、\t)
\S　――　非空白字符
\w　――　单词字符(字母、数字、和下划线)
\W　――　非单词字符
\x??――　16进制字符，应包含两位
. 　――　匹配任何字符
```

字符集合
```
[] 　――　多个字符组成的字符类，可以使用-字符指定字符范围（ASCII码顺序）
[^]　――　[]的反集
或
｜ 　――　用|分隔开的多个正则表达式间为或的关系
量词
*　  ――　匹配０次或多次
+  　――　匹配１次或多次
?  　――　匹配０次或１次
{n}　――　匹配ｎ次
{n,} ――　匹配至少n次
{n,m}――　匹配至少n次，至多m次
断言
^　――　匹配行首
$　――　匹配行尾
提取与分组
()  　――　分组模式并提取数据
(?:)　――　分组模式但不提取数据
```

##### 4. 流程驱动的应用
对于逻辑复杂的交互，流程驱动是比较自然的方法，流程驱动的程序主要使用arkco.sregex.Matcher类，本文开头的例子就是其典型的应用，以下对程序进行具体的解释：
```
Matcher ma = new Matcher(inputStream,buffer);
```

指定数据源，和数据缓冲（可选），
```
ma.setThrowTimeoutException(true);
```

指定Mathcer.find()方法在超时时抛出异常arkco.io.TimeoutException，如果设为false则返回-1,默认为false。如果超时不是异常，而是一种结果，通常不设置此值（即为false）以便参与程序逻辑。

```
String timeRegex="(\\d{4}-\\d\\d-\\d\\d\\s+\\d\\d:\\d\\d:\\d\\d)";
ma.add("COMMAND\\s+END\\s+"+timeRegex);
ma.add("COMMAND\\s+REJECTION\\s+"+timeRegex);
ma.add("COMMAND\\s+ERROR\\s+"+timeRegex);
send(“ADD SVC:DN=8881234,RV=TRUE;”);
switch (ma.find(timeout))
{...}
```

向Matcher添加模式，每个模式根据其添加的顺序各自对应一个序号，序号从０开始。当Matcher.find()方法匹配其中一个模式将返回其序号，程序根据序号进行相应处理。

##### 5. 数据驱动的应用
arkco.io.MatcheInputStream是为数据驱动的应用准备的，当在数据流中搜索到指定的数据将会触发IMatchedEvent事件，IMatchedEvent事件传递匹配的数据供程序处理。例程如下：

```
class Handler implements IMatchedEvent
{                                      
   public void onMatched(Submatched[][] matched,int id)
   {  switch(id)
      {  case 0:
            isHeight(matched[1][0]);
            break;
         case 1:
            isWeight(matched[1][0]);
            break;
}  }  }
class Test
{   ...
    InputStream inputStream;
    public void run()
   {  MatcheInputStream ma = new MatcheInputStream(inputStream,new Handler());
       ma.add(“height=(\d+)”);//id=0
       ma.add(“weight=(\d+)”);//id=1
       while(ma.read()>=0) { … }
}	}
```


Fuck CSDN Keywords: 习近平 8964
