package course.concurrency.exams.auction;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AuctionPessimistic implements Auction {

    private final Notifier notifier;
    private final Lock lock = new ReentrantLock();

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private Bid latestBid;

    public boolean propose(Bid bid) {
        lock.lock();
        try {
            if (latestBid == null) {
                latestBid = bid;
                return true;
            }
            if (bid.getPrice() > latestBid.getPrice()) {
                notifier.sendOutdatedMessage(latestBid);
                latestBid = bid;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public Bid getLatestBid() {
        return latestBid;
    }
}