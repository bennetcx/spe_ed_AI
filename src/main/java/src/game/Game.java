package src.game;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@SuppressWarnings("unused")
public class Game implements Cloneable
{
    private GameState state;

    private List<Player> players;
    private int ticks;
    private int currentPlayer = 0;
    private int firstPlayer = 0;
    private static final Random random = new Random();
    private final List<Integer> deaths = new ArrayList<>();
    private HashSet<ArrayList<Integer>> cellsChanged = new HashSet<>();

    public Game(GameState s, int ticks)
    {
        setState(s);
        this.ticks = ticks;
    }

    public static Game create(int width, int height, List<String> playerNames)
    {
        Random rand = random;
        GameState s = new GameState();
        s.cells = new int[height][width];

        for (int i = 0; i < height; ++i)
        {
            for (int j = 0; j < width; ++j)
            {
                s.cells[i][j] = 0;
            }
        }

        s.deadline = java.sql.Timestamp.valueOf(LocalDateTime.now());
        s.height = height;
        s.width = width;
        s.players = new HashMap<>();
        for (int i = 0; i < playerNames.size(); ++i)
        {
            PlayerState ps = new PlayerState();
            ps.x = 1 + rand.nextInt(width-2);
            ps.y = 1 + rand.nextInt(height-2);
            ps.speed = 1;
            ps.ID = i+1;
            ps.active = true;
            ps.name = playerNames.get(i);
            ps.direction = int2Direction(rand.nextInt(4));
            Player p = new Player(ps);
            s.players.put(String.valueOf(ps.ID), p.clone().getState());
        }
        s.you = 1;
        s.running = true;
        return new Game(s, 0);
    }

    // Creates a deep clone of this game
    public Game cloneGame()
    {
        GameState s = new GameState();

        s.cells = new int[getHeight()][getWidth()];
        for (int y = 0; y < getHeight(); ++y)
        {
            for (int x = 0; x < getWidth(); ++x)
            {
                s.cells[y][x] = getCells()[y][x];
            }
        }

        s.deadline = state.deadline;
        s.height = state.height;
        s.width = state.width;
        s.players = new HashMap<>();
        for (Map.Entry<String, PlayerState> entry : state.players.entrySet())
        {
            Player p = new Player(entry.getValue());
            s.players.put(entry.getKey(), p.clone().getState());
        }

        s.you = state.you;
        s.running = state.running;
        Game g = new Game(s, ticks);
        for (int death : deaths)
        {
            g.deaths.add(death);
        }
        g.setFirstPlayer(firstPlayer);
        return g;
    }

    public void performMove(GameMove move)
    {
        if (currentPlayer == firstPlayer)
        {
            ticks++;
        }

        Player p = players.get(currentPlayer);
        if (p.isActive())
        {
            // Map direction to int values of 0-3 (up left down right)
            Direction dir = p.getDirection();
            int num_dir = direction2Int(dir);

            if (move == GameMove.turn_left)
            {
                num_dir++;
                num_dir = num_dir % 4;
            }
            if (move == GameMove.turn_right)
            {
                num_dir--;
                num_dir = (num_dir + 4) % 4;
            }
            if (move == GameMove.speed_up)
            {
                p.getState().speed++;
            }
            if (move == GameMove.slow_down)
            {
                p.getState().speed--;
            }

            // Map back to direction
            p.setDirection(int2Direction(num_dir));

            // Move Player
            if (p.getSpeed() == 0)
            {
                killPlayer(currentPlayer);
            }

            boolean kill = false;
            for (int m = 0; m < p.getSpeed(); ++m)
            {
                int[] delta = direction2Delta(p.getDirection());
                p.getState().x += delta[0];
                p.getState().y += delta[1];

                if (!positionExists(p.getX(), p.getY()))
                {
                    killPlayer(currentPlayer);
                    break;
                }

                if(p.getSpeed() < 1 || p.getSpeed() > 10)
                {
                    killPlayer(currentPlayer);
                    break;
                }

                // speed >= 3 -> jump speed-2
                if (ticks % 6 == 0 && m != 0 && m != p.getSpeed() - 1)
                {
                    // Jump!
                    continue;
                }

                if(getCells()[p.getY()][p.getX()] != 0)
                {
                    if (getCells()[p.getY()][p.getX()] > 0)
                    {
                        ArrayList<Integer> l = new ArrayList<>();
                        l.add(p.getX());
                        l.add(p.getY());

                        if (cellsChanged != null && cellsChanged.contains(l))
                        {
                            // Kill the hit player too
                            killPlayer(getCells()[p.getY()][p.getX()]-1);
                        }
                    }
                    // Hit another player
                    killPlayer(currentPlayer);
                    getCells()[p.getY()][p.getX()] = -1;
                    break;
                }
                else
                {
                    getCells()[p.getY()][p.getX()] = currentPlayer+1;
                    ArrayList<Integer> l = new ArrayList<>();
                    l.add(p.getX());
                    l.add(p.getY());
                    cellsChanged.add(l);
                }
            }
        }

        currentPlayer++;
        currentPlayer %= getPlayerCount();
        if (currentPlayer == firstPlayer)
        {
            setRunning(false);
            for (Player pl : players)
            {
                if (pl.isActive())
                {
                    setRunning(true);
                }
            }
            cellsChanged.clear();
        }
    }

    // Perform a move for every player. Returns a new src.game.Game (deep copy)
    public Game variant(List<GameMove> moves) throws Exception
    {
        Game result = cloneGame();

        if (moves.size() != players.size()) {
            // Moves and player count don't match up
            // Return nothing
            throw new Exception("Player Moves dont match!");
        }

        if (!state.running)
        {
            // Return nothing
            return result;
        }

        result.tick(moves);
        return result;
    }

    // Applies moves to this object
    public void tick(List<GameMove> moves)
    {
        if (true)
        {
            int offset = currentPlayer;
            for (int i = 0; i < moves.size(); ++i)
            {
                int index = (offset + i) % getPlayerCount();
                performMove(moves.get(index));
            }
            return;
        }
        ticks++;
        // Index maps to player Index
        for (int i = 0; i < moves.size(); ++i) {
            Player p = players.get(i);

            // Map direction to int values of 0-3 (up left down right)
            Direction dir = p.getDirection();
            int num_dir = direction2Int(dir);

            if (moves.get(i) == GameMove.turn_left) {
                num_dir++;
                num_dir = num_dir % 4;
            }
            if (moves.get(i) == GameMove.turn_right) {
                num_dir--;
                num_dir = (num_dir + 4) % 4;
            }
            if (moves.get(i) == GameMove.speed_up) {
                p.getState().speed++;
            }
            if (moves.get(i) == GameMove.slow_down) {
                p.getState().speed--;
            }

            // Map back to direction
            p.setDirection(int2Direction(num_dir));
        }

        HashSet<ArrayList<Integer>> deathPoints = null;
        LinkedList<Integer> killList = new LinkedList<>();

        // Move Players
        for(int i = 0; i < players.size(); ++i)
        {
            Player p = players.get(i);

            // Legal state
            if (p.getSpeed() == 0)
            {
                killPlayer(i);
            }
            for (int m = 0; m < p.getSpeed(); ++m)
            {
                if(!p.isActive())
                {
                    break;
                }

                int[] delta = direction2Delta(p.getDirection());
                p.getState().x += delta[0];
                p.getState().y += delta[1];

                if (!positionExists(p.getX(), p.getY()))
                {
                    killPlayer(i);
                    break;
                }

                if(p.getSpeed() < 1 || p.getSpeed() > 10)
                {
                    killPlayer(i);
                    break;
                }

                // speed >= 3 -> jump speed-2
                if (ticks % 6 == 0 && m != 0 && m != p.getSpeed() - 1)
                {
                    // Jump!
                    continue;
                }

                // Check death
                if(getCells()[p.getY()][p.getX()] != 0)
                {
                    if (getCells()[p.getY()][p.getX()] > 0)
                    {
                        ArrayList<Integer> l = new ArrayList<>();
                        l.add(p.getX());
                        l.add(p.getY());

                        if (deathPoints != null && deathPoints.contains(l))
                        {
                            // Kill the hit player too
                            killList.add(getCells()[p.getY()][p.getX()]-1);
                        }
                    }
                    // Hit another player
                    killList.add(i);
                    getCells()[p.getY()][p.getX()] = -1;
                    break;
                }
                else
                {
                    if (deathPoints == null)
                    {
                        deathPoints = new HashSet<>();
                    }
                    getCells()[p.getY()][p.getX()] = i+1;
                    ArrayList<Integer> l = new ArrayList<>();
                    l.add(p.getX());
                    l.add(p.getY());
                    deathPoints.add(l);
                }
            }
        }

        // Kill players that intersected
        for (Integer p : killList)
        {
            killPlayer(p);
        }

        setRunning(false);
        for (Player p : players)
        {
            if (p.isActive())
            {
                setRunning(true);
            }
        }
    }

    public void killPlayer(int playerId)
    {
        if (!deaths.contains(playerId))
        {
            deaths.add(playerId);
        }
        players.get(playerId).setActive(false);
    }

    public boolean isDeadlyMove(int playerId, GameMove move)
    {
        Player player = players.get(playerId);
        if (!player.isActive())
        {
            return true;
        }
        int ticks = this.ticks + 1;

        // Map direction to int values of 0-3 (up left down right)
        Direction dir = player.getDirection();
        int speed = player.getSpeed();
        int px = player.getX();
        int py = player.getY();
        int num_dir = direction2Int(dir);

        if (move == GameMove.turn_left)
        {
            num_dir++;
            num_dir = num_dir % 4;
        }
        if (move == GameMove.turn_right)
        {
            num_dir--;
            num_dir = (num_dir + 4) % 4;
        }
        if (move == GameMove.speed_up)
        {
            speed++;
        }
        if (move == GameMove.slow_down)
        {
            speed--;
        }

        // Map back to direction
        dir = int2Direction(num_dir);

        // Move Player
        // Legal state
        if (speed == 0 || speed > 10)
        {
            return true;
        }

        for (int m = 0; m < speed; ++m)
        {
            int[] delta = direction2Delta(dir);
            px += delta[0];
            py += delta[1];
            if (!positionExists(px, py))
            {
                return true;
            }

            // speed >= 3 -> jump speed-2
            if (ticks % 6 == 0 && m != 0 && m != speed - 1)
            {
                // Jump!
                continue;
            }

            // else check death
            ArrayList<Integer> l = new ArrayList<>();
            l.add(px);
            l.add(py);

            if (getCells()[py][px] != 0)
            {
                return true;
            }
        }

        return false;
    }

    public int getDeath(int playerId)
    {
        return deaths.indexOf(playerId);
    }

    public int getDeaths(){
        return deaths.size();
    }

    public List<Integer> getDeadPlayersList(){
        return deaths;
    }

    public GameState getState() {
        return state;
    }

    public int getTicks()
    {
        return ticks;
    }

    // Sets the GameState and maps the players to int index
    // Maybe trivial but useful in the future
    public void setState(GameState state) {
        this.state = state;
        players = new ArrayList<>();
        for (int i = 0; i < state.players.size(); ++i)
        {
            Player player = new Player(state.players.get(String.valueOf(i+1)));
            players.add(player);
        }
    }

    public static Direction int2Direction(int num_dir)
    {
        return num_dir == 0 ? Direction.up :
                num_dir == 1 ? Direction.left :
                        num_dir == 2 ? Direction.down :
                                Direction.right;
    }

    public static int direction2Int(Direction dir)
    {
        return dir == Direction.up ? 0 :
                dir == Direction.left ? 1 :
                        dir == Direction.down ? 2 : 3;
    }

    public static int[] direction2Delta(Direction dir)
    {
        int x = 0, y = 0;
        switch (dir)
        {
            case up:
                --y;
                break;
            case left:
                --x;
                break;
            case down:
                ++y;
                break;
            case right:
                ++x;
                break;
        }
        int[] result = new int[2];
        result[0] = x;
        result[1] = y;
        return result;
    }

    public LocalDateTime getDeadline() {
        return state.deadline.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public int getWidth() {
        return state.width;
    }

    public void setWidth(int width) {
        state.width = width;
    }

    public int getHeight() {
        return state.height;
    }

    public void setHeight(int height) {
        state.height = height;
    }

    public int[][] getCells() {
        return state.cells;
    }

    public void setCells(int[][] cells) {
        state.cells = cells;
    }

    public Player getPlayer(int id)
    {
        return players.get(id);
    }

    public int getPlayerID(Player p)
    {
        return players.indexOf(p);
    }

    public int getPlayerCount()
    {
        return players.size();
    }

    public int getYou() {
        return state.you ;
    }

    public void setYou(int you) {
        state.you = you;
    }

    public boolean isRunning() {
        return state.running;
    }

    public void setRunning(boolean running) {
        state.running = running;
    }

    public void setDeadline(LocalDateTime deadline) {
        state.deadline = Timestamp.valueOf(deadline);
    }

    public boolean positionExists(int px, int py)
    {
        return (px < getWidth() && px >= 0 && py < getHeight() && py >= 0);
    }

    public int getCurrentPlayer()
    {
        return currentPlayer;
    }

    public int getFirstPlayer()
    {
        return firstPlayer;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setFirstPlayer(int firstPlayer)
    {
        if (currentPlayer == this.firstPlayer)
        {
            this.firstPlayer = firstPlayer;
            this.currentPlayer = firstPlayer;
        }
    }
}