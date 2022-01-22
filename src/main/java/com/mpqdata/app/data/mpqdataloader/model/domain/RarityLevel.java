package com.mpqdata.app.data.mpqdataloader.model.domain;

public enum RarityLevel {

	RARITY_1(1, 1, 50, null),
	RARITY_2(2, 15, 94, 144),
	RARITY_3(3, 40, 166, 266),
	RARITY_4(4, 70, 270, 370),
	RARITY_5(5, 255, 450, 550)
	;

	private final int rarity;
	private final int minLevel;
	private final int maxLevel;
	private final Integer maxChampLevel;

	private RarityLevel(int rarity, int minLevel, int maxLevel, Integer maxChampLevel) {
		this.rarity = rarity;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		this.maxChampLevel = maxChampLevel;
	}

	public int rarity() { return rarity ; }
	public int minLevel() { return minLevel ; }
	public int maxLevel() { return maxLevel ; }
	public Integer maxChampLevel() { return maxChampLevel ; }

}
