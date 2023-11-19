package course.concurrency.m2_async.cf.min_price;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PriceAggregator {

    private final Executor executor = Executors.newCachedThreadPool();

    private PriceRetriever priceRetriever = new PriceRetriever();
    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        List<CompletableFuture<Double>> completablePrices = shopIds.stream()
                .map(shopId -> CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), executor)
                        .completeOnTimeout(Double.NaN, 2960, TimeUnit.MILLISECONDS)
                        .exceptionally(ex -> Double.NaN))
                .collect(Collectors.toList());

        CompletableFuture.allOf(completablePrices.toArray(new CompletableFuture[0])).join();

        return completablePrices.stream()
                .map(CompletableFuture::join)
                .min(Double::compareTo)
                .orElse(Double.NaN);
    }
}
