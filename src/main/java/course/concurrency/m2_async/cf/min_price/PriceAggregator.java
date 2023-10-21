package course.concurrency.m2_async.cf.min_price;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        Executor executor = Executors.newFixedThreadPool(shopIds.size());

        List<CompletableFuture<Double>> completableFutures = shopIds.stream()
                .map(shopId -> supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), executor)
                        .orTimeout(3, TimeUnit.SECONDS))
                .toList();

        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
                .handleAsync((cf, throwable) -> completableFutures.stream()
                        .filter(f -> !f.isCompletedExceptionally())
                        .map(CompletableFuture::join)
                        .min(Double::compareTo)
                        .orElse(Double.NaN), executor)
                .join();
    }
}
