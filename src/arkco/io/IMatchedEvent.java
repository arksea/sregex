package arkco.io;

import arkco.sregex.Submatched;

/**
 * <p>Title: 匹配事件</p>
 * <p>Description: 匹配事件，与MatchInputStream联合使用</p>
 * @author 肖海星 arksea@gmail.com
 * @version 1.1.6
 */
public interface IMatchedEvent
{
    /**
     * @param matched Submatched[][] 匹配结果
     * @param id int 匹配模式索引
     */
    void onMatched(Submatched[][] matched,int id);
}
