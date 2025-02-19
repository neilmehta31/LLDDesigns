import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

interface User{
    String getUserId();
    boolean isPreferredBuyer();
    void incrementUserAuctionParticipationCount();
}

class Buyer implements User {
    private final String userId;
    private int userAuctionParticipationCount;
    private boolean isPreferredBuyer;

    public Buyer(String userId){
        this.userId = userId;
        this.isPreferredBuyer = false;
        this.userAuctionParticipationCount = 0;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public boolean isPreferredBuyer() {
        return isPreferredBuyer;
    }

    @Override
    public void incrementUserAuctionParticipationCount() {
        userAuctionParticipationCount++;
        if(userAuctionParticipationCount >= 2){
            setPreferredBuyer();
        }
    }

    public int getUserAuctionParticipationCount() {
        return userAuctionParticipationCount;
    }

    private void setPreferredBuyer() {
        isPreferredBuyer = true;
    }
}

class Seller implements User {
    private final String userId;

    public Seller(String userId){
        this.userId = userId;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public boolean isPreferredBuyer() {
        return false;
    }

    @Override
    public void incrementUserAuctionParticipationCount() {}
}

class AuctionBid{
    private Buyer buyer;
    private int amount;

    public AuctionBid(Buyer buyer, int amount) {
        this.buyer = buyer;
        this.amount = amount;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public void setBuyer(Buyer buyer) {
        this.buyer = buyer;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}

class Auction{
    private final String auctionId;
    private final int lowestBidAmount;
    private final int highestBidAmount;
    private final Seller seller;
    private Map<Buyer, AuctionBid> buyerAuctionBidMap;
    private boolean isAuctionLive;
    private final int participationCost;

    Auction(String auctionId, int lowestBidAmount, int highestBidAmount, Seller seller, int participationCost) {
        this.auctionId = auctionId;
        this.lowestBidAmount = lowestBidAmount;
        this.highestBidAmount = highestBidAmount;
        this.seller = seller;
        this.participationCost = participationCost;
        this.buyerAuctionBidMap = new HashMap<>();
        this.isAuctionLive = true;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public int getLowestBidAmount() {
        return lowestBidAmount;
    }

    public int getHighestBidAmount() {
        return highestBidAmount;
    }

    public Seller getSeller() {
        return seller;
    }

    public boolean isAuctionLive() {
        return isAuctionLive;
    }

    private void setAuctionClosed() {
        isAuctionLive = false;
    }

    public int getParticipationCost() {
        return participationCost;
    }

    public boolean createBid(Buyer buyer, int amount) {
        if (!isAuctionLive()){
            throw new IllegalStateException("Auction is already closed!");
        }

        if(amount < lowestBidAmount || amount > highestBidAmount){
            return false;
        }

        buyerAuctionBidMap.put(buyer, new AuctionBid(buyer, amount));
        return true;
    }

    public boolean updateBid(Buyer buyer, int newAmount) {
        if (!isAuctionLive()) {
            throw new IllegalStateException("Auction is closed");
        }

        if (!buyerAuctionBidMap.containsKey(buyer)) {
            return false;
        }

        return createBid(buyer, newAmount);
    }

    public boolean withdrawBid(Buyer buyer) {
        if(!isAuctionLive()){
            throw new IllegalStateException("Auction is already closed!");
        }
        if(!buyerAuctionBidMap.containsKey(buyer)){
            throw new IllegalArgumentException("Please put in a bid value before you try to withdraw yourself");
        }
        buyerAuctionBidMap.remove(buyer);
        return true;
    }

    public AuctionWinnerResult closeAuction() {
        if(!isAuctionLive()){
            throw new IllegalStateException("Auction is already closed!");
        }
        setAuctionClosed();
        incrementAllBuyerParticipation();
        return getAuctionWinner();
    }

    private void incrementAllBuyerParticipation(){
        for(AuctionBid auctionBid: buyerAuctionBidMap.values()){
            auctionBid.getBuyer().incrementUserAuctionParticipationCount();
        }
    }

    private AuctionWinnerResult getAuctionWinner(){
        Map<Integer, List<Buyer>> bidFrequency = new HashMap<>();
        for(AuctionBid auctionBid:buyerAuctionBidMap.values()){
            bidFrequency.computeIfAbsent(auctionBid.getAmount(), bidAmount -> new ArrayList<>()).add(auctionBid.getBuyer());
        }

        List<Integer> uniqueBids = bidFrequency.entrySet().stream()
                .filter(entry -> entry.getValue().size() == 1)
                .map(Map.Entry::getKey)
                .sorted(Comparator.reverseOrder())
                .toList();

        if(uniqueBids.isEmpty()){
            return new AuctionWinnerResult(null, 0);
        }

        int winningBid = uniqueBids.getFirst();
        List<Buyer> potentialWinners = bidFrequency.get(winningBid);

        Optional<Buyer> preferredWinner = potentialWinners.stream()
                .filter(Buyer::isPreferredBuyer)
                .findFirst();

        Buyer winner = preferredWinner.orElse(potentialWinners.getFirst());

        return new AuctionWinnerResult(winner, winningBid);
    }
}

class AuctionWinnerResult {
    private final Buyer winner;
    private final int winningBid;

    AuctionWinnerResult(Buyer winner, int winningBid) {
        this.winner = winner;
        this.winningBid = winningBid;
    }

    public int getWinningBid() {
        return winningBid;
    }


    public Buyer getWinner() {
        return winner;
    }
}

class AuctionManagementSystem{
    private Map<String, Buyer> buyers = new HashMap<>();
    private Map<String, Seller> sellers=  new HashMap<>();
    private Map<String, Auction> auctions = new HashMap<>();

    public void addBuyer(String buyerName) {
        buyers.put(buyerName, new Buyer(buyerName));
    }

    public void addSeller(String sellerName) {
        sellers.put(sellerName, new Seller(sellerName));
    }

    public void createAuction(String auctionId, int lowestBidLimit, int highestBidLimit,
                              int participationCost, String sellerName) {
        Seller seller = sellers.get(sellerName);
        if (seller == null) {
            throw new IllegalArgumentException("Cannot create auction as seller not found");
        }

        if(auctionId==null){
            auctionId = UUID.randomUUID().toString();
        }

        Auction auction = new Auction(auctionId, lowestBidLimit, highestBidLimit,
                seller, participationCost);
        auctions.put(auctionId, auction);
    }

    public boolean createBid(String buyerName, String auctionId, int amount) {
        Buyer buyer = buyers.get(buyerName);
        Auction auction = auctions.get(auctionId);

        if (buyer == null || auction == null) {
            return false;
        }

        return auction.createBid(buyer, amount);
    }

    public boolean updateBid(String buyerName, String auctionId, int newAmount) {
        Buyer buyer = buyers.get(buyerName);
        Auction auction = auctions.get(auctionId);

        if (buyer == null || auction == null) {
            return false;
        }

        return auction.updateBid(buyer, newAmount);
    }

    public boolean withdrawBid(String buyerName, String auctionId) {
        Buyer buyer = buyers.get(buyerName);
        Auction auction = auctions.get(auctionId);

        if (buyer == null || auction == null) {
            return false;
        }

        return auction.withdrawBid(buyer);
    }

    public AuctionWinnerResult closeAuction(String auctionId) {
        Auction auction = auctions.get(auctionId);

        if (auction == null) {
            throw new IllegalArgumentException("Auction not found");
        }

        return auction.closeAuction();
    }

}

public class FlipBidderMain {
    public static void main(String[] args) {
       AuctionManagementSystem ams = new AuctionManagementSystem();

//       TC - 1
        ams.addBuyer("buyer2");
        ams.addBuyer("buyer1");
        ams.addBuyer("buyer3");
        ams.addSeller("seller1");
        ams.createAuction("A1", 10, 50, 1, "seller1");
        ams.createBid("buyer1", "A1", 17);
        ams.createBid("buyer2", "A1", 15);
        ams.updateBid("buyer2", "A1", 19);
        ams.createBid("buyer3", "A1", 19);
        AuctionWinnerResult winner1 = ams.closeAuction("A1");
        System.out.println("Auction A1 winner : " + (winner1.getWinner() != null ? winner1.getWinner().getUserId() :
                "No winner :( "));


//        TC - 2
        ams.addSeller("seller2");
        ams.createAuction("A2", 5, 20, 2, "seller2");
        ams.createBid("buyer3", "A2", 25);
        ams.createBid("buyer2", "A2", 5);
        ams.withdrawBid("buyer2", "A2");
        AuctionWinnerResult winner2 = ams.closeAuction("A2");
        System.out.println("Auction A2 winner: " + (winner2.getWinner() != null ? winner2.getWinner().getUserId() :
                "No winner :( "));

        //        TC - 3
        ams.addSeller("seller3");
        ams.createAuction("A3", 5, 20, 2, "seller3");
        ams.createBid("buyer3", "A3", 19);
        ams.createBid("buyer2", "A3", 19);
        ams.createBid("buyer1", "A3", 19);
        ams.createBid("buyer4", "A3", 20); // doesnt let the buyer4 enter the auction cause its not registered
        ams.withdrawBid("buyer2", "A3");
        AuctionWinnerResult winner3 = ams.closeAuction("A3");
        System.out.println("Auction A3 winner: " + (winner3.getWinner() != null ? winner3.getWinner().getUserId() :
                "No winner :( "));
    }
}