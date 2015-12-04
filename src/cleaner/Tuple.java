package cleaner;

/**
 *
 * @author Babak Alipour (babak.alipour@gmail.com)
 */
public class Tuple {

    public int flow;
    public byte isCorrect;
    public byte errorCode;

    public Tuple(int flow, byte isCorrect, byte errorCode) {
        this.flow = flow;
        this.isCorrect = isCorrect;
        this.errorCode = errorCode;
    }
}
