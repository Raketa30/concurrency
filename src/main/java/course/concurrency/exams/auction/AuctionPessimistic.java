package course.concurrency.exams.auction;

public class AuctionPessimistic implements Auction {

    private final Notifier notifier;
    private final Object lock = new Object();

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
        this.latestBid = new Bid(0L, 0L, 0L);
    }

    private volatile Bid latestBid;

    public boolean propose(Bid bid) {
        if (bid.getPrice() < latestBid.getPrice()) {
            return false;
        }

        synchronized (lock) {
            if (bid.getPrice() > latestBid.getPrice()) {
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
}