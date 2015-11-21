package wtr.g8;

import wtr.sim.Point;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Player implements wtr.sim.Player {

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
	public void init(int id, int[] friend_ids, int strangers)
	{
		self_id = id;
		// initialize the wisdom array
		int N = friend_ids.length + strangers + 2;
		W = new int[N];
        Wb = new boolean[N];
		for (int i = 0 ; i != N ; ++i) {
            W[i] = (i == self_id) ? 0 : -1;
            Wb[i] = i == self_id;
        }
		for (int friend_id : friend_ids) {
            W[friend_id] = 50;
            Wb[friend_id] = true;
        }

        max_score = Math.min(50 * friend_ids.length + 200 + 20 * strangers, 1800);
        free_time = 1800 - max_score;
        System.out.println(self_id + "| max_score: " + max_score + " free_time: " + free_time);
	}

	// play function
	public Point play(Point[] players, int[] chat_ids,
	                  boolean wiser, int more_wisdom)
	{
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
            System.out.println(self_id + "| Not chatting with anyone :(");

            // find new chat buddy
            for (Point p : players) {
                // skip if no more wisdom to gain
                if (W[p.id] == 0) {
                    continue;
                }
                // compute squared distance
                double dx = self.x - p.x;
                double dy = self.y - p.y;
                double dd = dx * dx + dy * dy;
                // start chatting if in range
                if (dd >= 0.25 && dd <= 4.0) {
                    return new Point(0.0, 0.0, p.id);
                }
            }
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
