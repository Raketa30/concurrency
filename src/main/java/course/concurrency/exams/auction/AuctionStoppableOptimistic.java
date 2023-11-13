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
            boolean[] stoppedMark = new boolean[1];
            latestBid = atomicLatestBid.get(stoppedMark);
            if (stoppedMark[0] || bid.getPrice() < latestBid.getPrice()) {
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
        Bid latestBidReference = atomicLatestBid.getReference();
        atomicLatestBid.attemptMark(latestBidReference, true);
        return latestBidReference;
    }
}
