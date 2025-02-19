import java.util.HashMap;
import java.util.Map;
import javax.swing.text.Position;

public class Chess {

}

abstract class Piece{
    Position currentPosition;
    Map<Knight, String> umap = new HashMap<>();


    public Position getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Position currentPosition) {
        this.currentPosition = currentPosition;
        Knight knight = new Knight();
        umap.put(knight, "asdfs");
    }

    public void move(Position start, Position end){
        if(isValid(start, end)){
            setCurrentPosition(end);
        }
    }

    private boolean isValid(Position start, Position end) {
        return false;
    }
}

class LoggerMain{
    public static void main(String[] args){
        Chess chess = new Chess();
        
    }
}


class Knight{


}
