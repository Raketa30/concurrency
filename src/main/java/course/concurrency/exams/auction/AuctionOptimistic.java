package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private final Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicReference<Bid> atomicLatestBid = new AtomicReference<>(new Bid(0L, 0L, 0L));

    public boolean propose(Bid bid) {
        Bid latest;
        do {
            latest = atomicLatestBid.get();
            if (bid.getPrice() < latest.getPrice()) {
                return false;
            }
        } while (!atomicLatestBid.compareAndSet(latest, bid));
        notifier.sendOutdatedMessage(latest);
        return true;
    }

    public Bid getLatestBid() {
        return atomicLatestBid.get();
    }
}