import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * TODO:
 * - stack moving between piles
 * - change translator to map and allow keybindings
 */

public class Solitaire {
	private enum RANK {
		JOKER("?"),
		ACEL("A"),
		TWO("2"),
		THREE("3"),
		FOUR("4"),
		FIVE("5"),
		SIX("6"),
		SEVEN("7"),
		EIGHT("8"),
		NINE("9"),
		TEN("T"),
		JACK("J"),
		QUEEN("Q"),
		KING("K"),
		ACEH("A");
		private String sym;
		private RANK (String sym) { this.sym = sym; }
		public String toString () { return this.sym; }
	}
	private enum SUIT {
		SPADES("S",Color.BLACK),
		DIAMONDS("D",Color.RED),
		CLUBS("C",Color.BLACK),
		HEARTS("H",Color.RED);
		private String sym;
		private Color color;
		private SUIT (String sym, Color color) {
			this.sym = sym;
			this.color = color;
		}
		public Color getColor() { return this.color; }
		public String toString () { return this.sym; }
	}
	private class Card {
		private RANK rank;
		private SUIT suit;
		public Card (RANK rank, SUIT suit) {
			this.rank = rank;
			this.suit = suit;
		}
		public RANK getRank() { return this.rank; }
		public SUIT getSuit() { return this.suit; }
		public String toString() { return this.rank.toString() + this.suit.toString(); }
	}
	private class CardStack {
		private ArrayList<Card> cards;
		private ArrayList<Boolean> faces;
		public CardStack () {
			this.cards = new ArrayList<Card>();
			this.faces = new ArrayList<Boolean>();
		}
		public int size () { return this.cards.size(); }
		public Card getTopCard () {
			Card result = null;
			if (this.cards.size() > 0) {
				result = this.cards.get(this.cards.size()-1);
			}
			return result;
		}
		public void add (Card card, boolean faceUp) {
			this.cards.add(card);
			this.faces.add(faceUp);
		}
		public Card get (int i) { return this.cards.get(i); }
		public boolean isFaceUp(int i) { return this.faces.get(i); }
		public void move (int num, CardStack dest) {
			for (int i = 0; i < num; i++) {
				dest.cards.add(this.cards.remove(this.cards.size()-1));
				dest.faces.add(this.faces.remove(this.faces.size()-1));
			}
		}
		public void flip (int num, CardStack dest) {
			for (int i = 0; i < num; i++) {
				dest.cards.add(this.cards.remove(this.cards.size()-1));
				dest.faces.add(!this.faces.remove(this.faces.size()-1));
			}
		}
		public void shuffle () {
			for (int i = 0; i < 100000; i++) {
				this.cards.add((int)(Math.random()*this.cards.size()),this.cards.remove(this.cards.size()-1));
				this.faces.add((int)(Math.random()*this.faces.size()),this.faces.remove(this.faces.size()-1));
			}
		}
	}
	private class Game {
		private HashMap<String,CardStack> board;
		private static final int NUM_PILES = 7;
		private static final int NUM_CYCLE = 3;
		private static final String DECK = "deck", OPEN = "open", PILE = "pile", HOUSE = "house";
		private String[] translator;
		public Game () {
			this.board = new HashMap<String,CardStack>();
			this.board.put(Game.DECK, new CardStack());
			this.board.put(Game.OPEN, new CardStack());
			for (SUIT s : SUIT.values()) {
				this.board.put(Game.HOUSE+s, new CardStack());
			}
			for (int i = 0; i < Game.NUM_PILES; i++) {
				this.board.put(Game.PILE+i, new CardStack());
			}
			for (SUIT s : SUIT.values()) {
				for (RANK r : RANK.values()) {
					if (r != RANK.JOKER && r != RANK.ACEH) {
						this.board.get(Game.DECK).add(new Card(r,s), false);
					}
				}
			}
			this.board.get(Game.DECK).shuffle();
			for (int i = 0; i < Game.NUM_PILES; i++) {
				for (int j = 0; j < Game.NUM_PILES - i; j++) {
					this.board.get(Game.DECK).move(1, this.board.get(Game.PILE+j));
				}
				this.board.get(Game.PILE+(Game.NUM_PILES - i - 1)).flip(1, this.board.get(Game.PILE+(Game.NUM_PILES - i - 1)));
			}
			this.translator = new String[1+Game.NUM_PILES];
			this.translator[0] = Game.OPEN;
			for (int i = 0; i < Game.NUM_PILES; i++) {
				this.translator[i+1] = Game.PILE + i;
			}
		}
		private CardStack getMatchingHouse(Card card) {
			return this.board.get(Game.HOUSE + card.getSuit());
		}
		public void cycle () {
			if (this.board.get(Game.DECK).size() == 0) {
				this.board.get(Game.OPEN).flip(this.board.get(Game.OPEN).size(), this.board.get(Game.DECK));
			}
			else {
				int i = 0;
				while (i < Game.NUM_CYCLE && this.board.get(Game.DECK).size() > 0) {
					this.board.get(Game.DECK).flip(1, this.board.get(Game.OPEN));
					i++;
				}
			}
		}
		//move to houses only
		public boolean canMove (int src) {
			return
					src >= 0 && src < translator.length
					&& this.board.containsKey(translator[src])
					&& this.board.get(translator[src]).size() > 0
					&& (
							this.board.get(translator[src]).getTopCard().getRank() == RANK.ACEL && this.getMatchingHouse(this.board.get(translator[src]).getTopCard()).size() == 0
							|| this.board.get(translator[src]).getTopCard().getRank().compareTo(this.getMatchingHouse(this.board.get(translator[src]).getTopCard()).getTopCard().getRank()) == 1
							);
		}
		public void move (int src) {
			if (this.canMove(src)) {
				this.board.get(translator[src]).move(1, this.board.get(Game.HOUSE + this.board.get(translator[src]).get(this.board.get(translator[src]).size()-1).getSuit()));
			}
		}
		//moves not involving houses
		public boolean canMove (int src, int dest) {
			return
					src >= 0 && src < translator.length
					&& dest >= 0 && dest < translator.length
					&& !translator[dest].equals(Game.OPEN)
					&& this.board.containsKey(translator[src])
					&& this.board.containsKey(translator[dest])
					&& this.board.get(translator[src]).size() > 0
					&& (
							this.board.get(translator[src]).getTopCard().getRank() == RANK.KING && this.board.get(translator[dest]).size() == 0
							|| (
									this.board.get(translator[dest]).size() > 0
									&& this.board.get(translator[src]).getTopCard().getSuit().getColor() != this.board.get(translator[dest]).getTopCard().getSuit().getColor()
									&& this.board.get(translator[src]).getTopCard().getRank().compareTo(this.board.get(translator[dest]).getTopCard().getRank()) == -1
									)
							);
		}
		public void move (int src, int dest) {
			if (this.canMove(src,dest)) {
				this.board.get(translator[src]).move(1, this.board.get(translator[dest]));
			}
		}
		public String toString() {
			StringBuilder result = new StringBuilder();
			
			result.append(this.board.get(Game.DECK).size() < 10 ? " " : "");
			result.append(this.board.get(Game.DECK).size());
			result.append("[" + (this.board.get(Game.DECK).size() > 0 ? "XX" : "  ") + "]");
			result.append(" ");
			result.append("[" + (this.board.get(Game.OPEN).size() > 0 ? this.board.get(Game.OPEN).getTopCard() : "  ") + "]");
			result.append(" ");
			result.append("    ");
			for (SUIT s : SUIT.values()) {
				result.append(" [" + (this.board.get(Game.HOUSE+s).size() > 0 ? this.board.get(Game.HOUSE+s).getTopCard() : "  ") + "]");
			}
			result.append("\r\n\r\n");
			result.append(" ");
			int largestPileSize = 0;
			for (int i = 0; i < Game.NUM_PILES; i++) {
				result.append("  " + (i < 10 ? " " : "") + i + " ");
				largestPileSize = Math.max(largestPileSize, this.board.get(Game.PILE+i).size());
			}
			for (int i = 0; i < largestPileSize; i++) {
				result.append("\r\n ");
				for (int j = 0; j < Game.NUM_PILES; j++) {
					if (i < this.board.get(Game.PILE+j).size()) {
						result.append(" [" + (this.board.get(Game.PILE+j).isFaceUp(i) ? this.board.get(Game.PILE+j).get(i) : "XX") + "]");
					}
					else {
						result.append("     ");
					}
				}
			}
			
			return result.toString();
		}
	}
	public static void main (String[] args) {
		Solitaire self = new Solitaire();
		Game g = self.new Game();
		System.out.println(g);
		g.cycle();
		g.move(1);
		g.move(1,2);
	}
}
