package wtr.g6;

import java.util.ArrayList;
import wtr.sim.Point;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class WisdomComparator implements Comparator<Person>{

	// In the future, add more attributes here as we make this logic more sophisticated!

    Person self;
    Point[] players;

    public WisdomComparator(Person self, Point[] players) {
        this.self = self;
        this.players = players;
    }

    @Override
    public int compare(Person p1, Person p2) {
    	//System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        double distance1 = Utils.distance(self.cur_position, p1.cur_position);
        double distance2 = Utils.distance(self.cur_position, p2.cur_position);
        boolean free1 = p1.chat_id == p1.id;
        boolean free2 = p2.chat_id == p2.id;
        int wisdom1 = p1.wisdom == -1 ? 100 : p1.wisdom;
        int wisdom2 = p2.wisdom == -1 ? 100 : p2.wisdom;
//        int wisdom1 = p1.wisdom;
//        int wisdom2 = p2.wisdom;

//        return wisdom2 - wisdom1;
//
//        if(free1 && !free2) {
//            return -1;
//        }
//        if(!free1 && free2) {
//            return 1;
//        }

        double cost1 = cost(distance1, wisdom1, free1, p1);
        double cost2 = cost(distance2, wisdom2, free2, p2);

        if(cost1 == cost2) {
            return 0;
        }
        return cost1 > cost2 ? -1 : 1;
    }

    private double cost(double distance, int wisdom, boolean free, Person p) {
        try {
            double scale = free ? 100 : 1;
            // whether there is some one too close to that person
            double dmin = Utils.closestPersonDist(players, p, self);
            double scale2 = dmin <= 0.5 ? 0.5 : 1;
            double scale3 = distance>6 ? -1 : distance;
            return (scale2 * scale * wisdom) / scale3;
        } catch (Exception e) {
            return 0;
        }
    }
}
