package arkco.sregex;

import junit.framework.TestCase;

public class TestPattern extends TestCase
{
    private Pattern pattern = null;

    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    protected void tearDown()
        throws Exception
    {
        pattern = null;
        super.tearDown();
    }

    private void initPattern(String regex)
        throws PatternCompiler_E
    {
        pattern = new Pattern(regex);
    }

    public void testSearchStopIndex()
    {
        try
        {
            initPattern("(ABCD)*577");
            int index = pattern.getStopOffset(7);
            assertEquals("return value", 7, index);

            initPattern("((ab\\d+|ab\\s+)*|577)long");
            index = pattern.getStopOffset(6);
            assertEquals("return value", 10, index);

            index = pattern.getStopOffset(11);
            assertEquals("return value", 15, index);
        }
        catch (PatternCompiler_E ex)
        {
            ex.printStackTrace();
            assertTrue("正则表达式错误", false);
        }
    }
}
