package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private final Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicReference<Bid> atomicLatestBid = new AtomicReference<>();

    public boolean propose(Bid bid) {
        if (atomicLatestBid.compareAndSet(null, bid)) {
            return true;
        }
        while(true) {
            Bid latest = atomicLatestBid.get();
            if (bid.getPrice() > latest.getPrice()) {
                if (atomicLatestBid.compareAndSet(latest, bid)) {
                    notifier.sendOutdatedMessage(latest);
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    public Bid getLatestBid() {
        return atomicLatestBid.get();
    }
}