package arkco.sregex;

/**
 * 字符类标记
 */
final class Metacode
{
    static final String def = ".?+*|()[{\\^$";
    static final String set = "-]\\^";
}

final class CharType
{
    static final int
        NOT_CHAR = -1, //非有效字符
        //字符类
        //ASCII = 0--255
        ANY = 256, //.
        DIGIT = 257, //\d
        NDIGIT = 258, //\D
        WORD = 259, //\w
        NWORD = 260, //\W
        SPACE = 261, //\s
        NSPACE = 262, //\S
        EOF   = 263; //\0
}
