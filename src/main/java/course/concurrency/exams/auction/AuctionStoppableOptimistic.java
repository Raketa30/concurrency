package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private final Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicMarkableReference<Bid> atomicLatestBid =
            new AtomicMarkableReference<>(new Bid(0L, 0L, 0L), false);

    public boolean propose(Bid bid) {
        Bid latestBid;
        do {
            latestBid = atomicLatestBid.getReference();
            if (atomicLatestBid.isMarked() || bid.getPrice() < latestBid.getPrice()) {
                return false;
            }
        } while (!atomicLatestBid.compareAndSet(latestBid, bid, false, false));
        notifier.sendOutdatedMessage(latestBid);
        return true;
    }

    public Bid getLatestBid() {
        return atomicLatestBid.getReference();
    }

    public Bid stopAuction() {
        do {
        } while (!atomicLatestBid.attemptMark(atomicLatestBid.getReference(), true));
        return atomicLatestBid.getReference();
    }
}
