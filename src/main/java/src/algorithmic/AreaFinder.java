package src.algorithmic;

import src.game.Direction;
import src.game.Game;
import src.game.Player;

import java.util.HashSet;

public class AreaFinder
{
    //private List<Field>[] nConnected;
    //private Set<Connector>[] nAreas;
    private final Field[][] collected;
    private Game game;
    private int areaLeft;

    public AreaFinder(Game game)
    {
        this.game = game;
        Connector.initialCapacity = game.getWidth() * game.getHeight();
        collected = new Field[game.getHeight()][game.getWidth()];
    }

    private void connectAt(int x, int y)
    {
        for (int i = 0; i < 2; ++i)
        {
            int[] dxy = Game.direction2Delta(Game.int2Direction(i));
            if (    game.positionExists(x + dxy[0], y + dxy[1]) &&
                    game.getCells()[y + dxy[1]][x + dxy[0]] == 0 /*&&
                    collected[y + dxy[1]][x + dxy[0]].getConnections() >=
                            collected[y][x].getConnections()*/
                )
            {
                Connector.merge(collected[y + dxy[1]][x + dxy[0]], collected[y][x]);
            }
        }
    }

    public void setGame(Game g)
    {
        this.game = g;
    }

    public int countConnections(int x, int y)
    {
        int counter = 0;
        for (int i = 0; i < 4; ++i)
        {
            int[] dxy = Game.direction2Delta(Game.int2Direction(i));
            if (game.positionExists(x + dxy[0], y + dxy[1]) &&
                    game.getCells()[y + dxy[1]][x + dxy[0]] == 0)
            {
                counter++;
            }
        }
        return counter;
    }

    public void findAreas()
    {
        findAreas(false);
    }

    public void findAreas(boolean useC)
    {
        //nConnected = new List[4];
        //nConnected[0] = new LinkedList<>();
        //nConnected[1] = new LinkedList<>();
        //nConnected[2] = new LinkedList<>();
        //nConnected[3] = new LinkedList<>();
        //nAreas = new Set[4];
        //nAreas[0] = new HashSet<>();
        //nAreas[1] = new HashSet<>();
        //nAreas[2] = new HashSet<>();
        //nAreas[3] = new HashSet<>();
        areaLeft = 0;

        for (int x = 0; x < game.getWidth(); ++x)
        {
            for (int y = 0; y < game.getHeight(); ++y)
            {
                if (game.getCells()[y][x] == 0)
                {
                    areaLeft++;
                    int c = countConnections(x, y);
                    collected[y][x] = new Field(0, c, x, y);
                    collected[y][x].setLeafCount(useC ? c : 1);
                    //if (c > 0)
                    //{
                    //    nConnected[c-1].add(collected[y][x]);
                    //}
                    connectAt(x, y);
                }
                else
                {
                    collected[y][x] = null;
                }
            }
        }

        //for (int i = 3; false && i >= 0; --i)
        //{
        //    Set<Connector> grab = new HashSet<>();
        //    for (Field f : nConnected[i])
        //    {
        //        connectAt(f.getX(), f.getY());
        //        grab.add(f.getHead());
        //    }
        //    Set<Connector> areas = new HashSet<>();
        //    for (Connector c : grab)
        //    {
        //        Connector next = c;
        //        if (!c.isSealed())
        //        {
        //            next = c.getHead();
        //        }
//
        //        //next.setSealed();
        //        areas.add(next);
        //    }
//
        //    nAreas[i] = areas;
        //}
    }

    public Field[][] getCollected()
    {
        return collected;
    }

    public int getAreaAt(int px, int py)
    {
        if (!game.positionExists(px, py) || game.getCells()[py][px] != 0)
        {
            return 0;
        }
        return getClumpAt(px, py).getLeafCount();
    }

    public Field getFieldAt(int px, int py)
    {
        return collected[py][px];
    }

    public Connector getClumpAt(int px, int py)
    {
        if (!game.positionExists(px, py))
        {
            return null;
        }
        if (collected == null || collected[py][px] == null)
        {
            return null;
        }
        return collected[py][px].getLastHead();
    }

    public int getMaxAreaAround(int playerId)
    {
        Player p = game.getPlayer(playerId);
        return getMaxAreaAround(p.getX(), p.getY());
    }

    public int getMaxAreaAround(int px, int py)
    {
        int max = 0;
        for (int i = 0; i < 4; ++i)
        {
            Direction dir = Game.int2Direction(i);
            int[] dxy = Game.direction2Delta(dir);
            int x = px + dxy[0];
            int y = py + dxy[1];
            if (game.positionExists(x, y))
            {
                int eval = getAreaAt(x, y);
                if (eval > max)
                {
                    max = eval;
                }
            }
        }

        return max;
    }

    public double calculateOpenness(int playerId)
    {
        // TODO: Evaluate 4-Connections
        // Simple: Game Space
        return (double) getMaxAreaAround(playerId) / (game.getWidth() * game.getHeight());
    }

    public double getReachableGameArea()
    {
        HashSet<Connector> areas = new HashSet<>();

        for (int p = 0; p < game.getPlayerCount(); ++p)
        {
            Player pl = game.getPlayer(p);
            for (int i = 0; i < 4; ++i)
            {
                Direction dir = Game.int2Direction(i);
                int[] dxy = Game.direction2Delta(dir);
                int x = pl.getX() + dxy[0];
                int y = pl.getY() + dxy[1];
                if (game.positionExists(x, y))
                {
                    areas.add(getClumpAt(x, y));
                }
            }
        }

        double allAreas = 0;
        for (Connector area : areas)
        {
            if (area != null)
            {
                allAreas += area.getLeafCount();
            }
        }

        return allAreas;
    }

    public int getAreaLeft()
    {
        return areaLeft;
    }

    public void setAreaLeft(int areaLeft)
    {
        this.areaLeft = areaLeft;
    }
}
