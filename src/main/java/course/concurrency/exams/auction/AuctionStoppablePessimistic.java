package course.concurrency.exams.auction;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private final Notifier notifier;
    private final Object lock = new Object();
    private volatile Bid latestBid;
    private volatile boolean stopped;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
        this.latestBid = new Bid(0L, 0L, 0L);
        this.stopped = false;
    }

    public boolean propose(Bid bid) {
        if (stopped || bid.getPrice() < latestBid.getPrice()) {
            return false;
        }

        synchronized (lock) {
            if (!stopped && bid.getPrice() > latestBid.getPrice()) {
                latestBid = bid;
                notifier.sendOutdatedMessage(latestBid);
                return true;
            }
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }

    public Bid stopAuction() {
        synchronized (lock) {
            stopped = true;
        }
        return latestBid;
    }
}
