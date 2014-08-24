package erogenousbeef.bigreactors.api.data;

public class SourceProductMapping {

	protected String source;
	protected String product;
	protected int sourceAmount;
	protected int productAmount;
	
	/**
	 * Maps a source item to a product item, with quantities attached.
	 * @param source The key of the source item, e.g. oredict name, fluidict name or reactant name
	 * @param product The key of the product item, e.g. oredict name, fluidict name or reactant name
	 * @param sourceAmount The amount of source stuff used
	 * @param productAmount The amount of product stuff produced
	 */
	public SourceProductMapping(String sourceKey, int sourceAmount, String productKey, int productAmount) {
		if(sourceKey == null) { throw new IllegalArgumentException("Cannot create mapping with null source name string"); }
		if(productKey == null) { throw new IllegalArgumentException("Cannot create mapping with null product name string"); }
		if(sourceAmount <= 0) { throw new IllegalArgumentException("Cannot create mapping which consumes less than 1 unit of source item"); }
		if(productAmount <= 0) { throw new IllegalArgumentException("Cannot create mapping which produces less than 1 unit of product item"); }

		this.source  = sourceKey;
		this.product = productKey;
		this.sourceAmount = sourceAmount;
		this.productAmount= productAmount;
	}
	
	public String getSource() { return source; }
	public String getProduct() { return product; }
	public int getSourceAmount() { return sourceAmount; }
	public int getProductAmount() { return productAmount; }
	
	public SourceProductMapping getReverse() {
		return new SourceProductMapping(product, productAmount, source, sourceAmount);
	}
	
	/**
	 * Returns the amount of product which can be produced from a given quantity
	 * of the source thing.
	 * If there is not enough of the source item, returns zero.
	 * @param sourceQty The amount of source thing available.
	 * @return The amount of product which can be produced. May be 0.
	 */
	public int getProductAmount(int sourceQty) {
		return (sourceQty / sourceAmount) * productAmount;
	}
	
	/**
	 * Returns the amount of source needed to produce a given quantity of product.
	 * Note that this may not produce the full amount you requested; you should
	 * check the result with getProductAmount() afterwards to see how much need be consumed.
	 * @param productQty The amount of product to produce
	 * @return The amount of source needed to produce at most productQty units of the product.
	 */
	public int getSourceAmount(int productQty) {
		return (productQty / productAmount) * sourceAmount;
	}
	
}
