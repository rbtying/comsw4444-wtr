package wtr.g8;

import wtr.sim.Point;

import java.util.*;

public class Player implements wtr.sim.Player {

    private static final int MLE_ENEMY_POINTS = 6;
    // your own id
    private int self_id = -1;
    private int soulmate_id = -1;
    // the remaining wisdom per player
    private int[] W = null;
    private boolean[] Wb = null;
    private int max_score;
    private int free_time;
    // random generator
    private Random random = new Random();

    // init function called once
    public void init(int id, int[] friend_ids, int strangers) {
        self_id = id;
        // initialize the wisdom array
        int N = friend_ids.length + strangers + 2;
        W = new int[N];
        Wb = new boolean[N];
        for (int i = 0; i != N; ++i) {
            W[i] = (i == self_id) ? 0 : MLE_ENEMY_POINTS;
            Wb[i] = i == self_id;
        }
        Wb[self_id] = true;

        for (int friend_id : friend_ids) {
            W[friend_id] = 50;
            Wb[friend_id] = true;
        }

        max_score = Math.min(50 * friend_ids.length + 200 + 20 * strangers, 1800);
        free_time = 1800 - max_score;
        System.out.println(self_id + "| max_score: " + max_score + " free_time: " + free_time);
    }

    private double dist(Point a, Point b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    private boolean inRange(Point a, Point b, double mindist, double maxdist) {
        double d = dist(a, b);
        return d >= mindist && d <= maxdist;
    }

    private boolean inRange(Point a, Point b) {
        return inRange(a, b, 0.5, 2.0);
    }

    private int compare(Point a, Point b, Map<Integer, Integer> chat_lut) {
        int chata = chat_lut.get(a.id);
        int chatb = chat_lut.get(b.id);

        return Integer.compare(W[b.id] + 500 * Integer.signum(chatb), W[a.id] + 500 * Integer.signum(chata));
    }

    private Point teleportToHighestScore(Point players[], Map<Integer, Integer> id_lut,
                                         Map<Integer, Integer> chat_lut) {
        if (players.length == 0) {
            return null;
        }

        List<Point> l = new ArrayList<>();

        for (Point p : players) {
            if (W[p.id] == 0) {
                continue;
            }
            l.add(p);
        }

        if (l.isEmpty()) {
            return null;
        }

        Collections.sort(l, (a, b) -> compare(a, b, chat_lut));

        Point tgt = l.get(0);
        Point self = players[id_lut.get(self_id)];

        // compute a point that is 0.5 away
        double dx = tgt.x - self.x;
        double dy = tgt.y - self.y;
        double dist = Math.hypot(dx, dy);
        double th = Math.atan2(dy, dx);

        return new Point((dist - 0.55) * Math.cos(th), (dist - 0.55) * Math.sin(th), self_id);
    }

    // play function
    public Point play(Point[] players, int[] chat_ids,
                      boolean wiser, int more_wisdom) {
        Map<Integer, Integer> id_lut = new HashMap<>(); // :: id -> index
        Map<Integer, Integer> chat_lut = new HashMap<>(); // :: id -> id

        for (int i = 0; i < players.length; ++i) {
            id_lut.put(players[i].id, i);
            if (chat_ids[i] == players[i].id) {
                chat_lut.put(players[i].id, -1);
            } else {
                chat_lut.put(players[i].id, chat_ids[i]);
            }
        }

        int player_index = id_lut.get(self_id);
        Integer chat_id = chat_lut.get(self_id);

        // find where you are and who you chat with

        Point self = players[player_index];

        if (chat_id < 0) {
            // not chatting with anyone right now!
//            System.out.println(self_id + "| Not chatting with anyone :(");

            List<Point> very_close = new ArrayList<>();

            // find new chat buddy
            for (Point p : players) {
                // skip if no more wisdom to gain
                if (W[p.id] == 0) {
                    continue;
                }

                // compute squared distance
                // start chatting if in range
                if (inRange(self, p, 0.5, 2.0)) {
                    very_close.add(p);
                }
            }

            Collections.sort(very_close, (a, b) -> compare(a, b, chat_lut));
            if (!very_close.isEmpty()) {
                return new Point(0, 0, very_close.get(0).id);
            }

            // if we're here, we need to search further
            // very_close is empty
//            Point teleport = teleportToHighestScore(players, id_lut, chat_lut);
//            if (teleport != null) {
//                return teleport;
//            }
        } else {
            // record known wisdom
            W[chat_id] = more_wisdom;
            if (more_wisdom > 50 && !Wb[chat_id]) {
                soulmate_id = chat_id;
                System.out.println(self_id + "| found soulmate : " + soulmate_id);
            }
            Wb[chat_id] = true;

            // attempt to continue chatting if there is more wisdom
            if (wiser) {
                return new Point(0.0, 0.0, chat_id);
            } else {
                Point teleport = teleportToHighestScore(players, id_lut, chat_lut);
                if (teleport != null) {
                    return teleport;
                }
            }
        }

        --free_time;

        // return a random move
        double dir = random.nextDouble() * 2 * Math.PI;
        double dx = 6 * Math.cos(dir);
        double dy = 6 * Math.sin(dir);
        return new Point(dx, dy, self_id);
    }
}
