package arkco.io;

import arkco.sregex.Submatched;

/**
 * <p>Title: ƥ���¼�</p>
 * <p>Description: ƥ���¼�����MatchInputStream����ʹ��</p>
 * @author Ф���� arksea@gmail.com
 * @version 1.1.6
 */
public interface IMatchedEvent
{
    /**
     * @param matched Submatched[][] ƥ����
     * @param id int ƥ��ģʽ����
     */
    void onMatched(Submatched[][] matched,int id);
}
