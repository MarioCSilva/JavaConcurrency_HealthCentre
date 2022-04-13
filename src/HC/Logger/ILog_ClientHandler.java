package HC.Logger;

public interface ILog_ClientHandler {
    void write(String msg);
    void writeHeaders();
    void writeState(String msg);
}
