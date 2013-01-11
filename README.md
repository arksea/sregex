sregex
======

A java regex lib, It's non-backtracking and for data streams


3. 模式简介 
3.1. 
非贪婪匹配 
流式模式匹配基本兼容Perl，但由于流式数据不能预知数据的特点，流式模式匹配中的量词　? * + {m,n}　为非贪婪匹配，即分别相当于Perl的　??  *?  +?  {m,n}? 　。请看以下例子： 

对于数据流：　abc_abc_abc 

\w+_　将匹配　abc_ 而不是　abc_abc_ 就相当于Perl的 \w+?_ 

对于数据流：name=John;height=173;weight=63;age=27; 

height=\d+  将匹配　height=173 而不是　height=1,因为\d+是最后一个模式，没有结束标记，所以它是以非数字作为结束判断。



3.2. 
可展开的提取 
arkco.sregex的提取类似于boost.regex是可展开的，这在很多情境下相当方便。 

3.3. 
模式列表 
1）字符 

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

字符集合 

[] 　――　多个字符组成的字符类，可以使用-字符指定字符范围（ASCII码顺序） 

[^]　――　[]的反集 

或 

｜　――　用|分隔开的多个正则表达式间为或的关系 

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



4. 流程驱动的应用 
对于逻辑复杂的交互，流程驱动是比较自然的方法，流程驱动的程序主要使用arkco.sregex.Matcher类，本文开头的例子就是其典型的应用，以下对程序进行具体的解释： 

Matcher ma = new Matcher(inputStream,buffer); 

指定数据源，和数据缓冲（可选）， 



ma.setThrowTimeoutException(true); 

指定Mathcer.find()方法在超时时抛出异常arkco.io.TimeoutException，如果设为false则返回-1,默认为false。如果超时不是异常，而是一种结果，通常不设置此值（即为false）以便参与程序逻辑。 

String timeRegex="(\\d{4}-\\d\\d-\\d\\d\\s+\\d\\d:\\d\\d:\\d\\d)"; 

ma.add("COMMAND\\s+END\\s+"+timeRegex); 

ma.add("COMMAND\\s+REJECTION\\s+"+timeRegex); 

ma.add("COMMAND\\s+ERROR\\s+"+timeRegex); 

send(“ADD SVC:DN=8881234,RV=TRUE;”); 

switch (ma.find(timeout)) 

{...} 

向Matcher添加模式，每个模式根据其添加的顺序各自对应一个序号，序号从０开始。当Matcher.find()方法匹配其中一个模式将返回其序号，程序根据序号进行相应处理。 

5. 数据驱动的应用 
arkco.io.MatcheInputStream是为数据驱动的应用准备的，当在数据流中搜索到指定的数据将会触发IMatchedEvent事件，IMatchedEvent事件传递匹配的数据供程序处理。例程如下： 

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

}    } 
