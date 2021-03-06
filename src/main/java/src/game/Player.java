package src.game;

public class Player implements Cloneable
{
    private final PlayerState state;
    private int score = 0;

    public PlayerState getState() {
        return state;
    }

    public int getID() {
        return state.ID;
    }

    public void setID(int ID) {
        state.ID = ID;
    }

    public int getX() {
        return state.x;
    }

    public void setX(int x) {
        state.x = x;
    }

    public int getY() {
        return state.y;
    }

    public void setY(int y) {
        state.y = y;
    }

    public Direction getDirection() {
        return state.direction;
    }

    public int getNextX()
    {
        int[] dxy = Game.direction2Delta(getDirection());
        return getX() + dxy[0];
    }

    public int getNextY()
    {
        int[] dxy = Game.direction2Delta(getDirection());
        return getY() + dxy[1];
    }

    public void setDirection(Direction direction) {
        state.direction = direction;
    }

    public int getSpeed() {
        return state.speed;
    }

    public void setSpeed(int speed) {
        state.speed = speed;
    }

    public boolean isActive() {
        return state.active;
    }

    public void setActive(boolean active) {
        state.active = active;
    }

    public String getName() {
        return state.name;
    }

    public void setName(String name) {
        state.name = name;
    }

    public double getDistanceTo(Player other)
    {
        double dx = this.getX() - other.getX();
        double dy = this.getY() - other.getY();
        return Math.sqrt(dx*dx+dy*dy);
    }

    @Override
    public Player clone() {
        PlayerState s = new PlayerState();
        Player result = new Player(s);
        result.setActive(isActive());
        result.setDirection(getDirection());
        result.setID(getID());
        result.setName(getName());
        result.setSpeed(getSpeed());
        result.setX(getX());
        result.setY(getY());
        result.setScore(getScore());
        return result;
    }

    public Player(PlayerState state)
    {
        this.state = state;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
