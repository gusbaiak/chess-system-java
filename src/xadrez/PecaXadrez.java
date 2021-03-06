package xadrez;

import tabuleiro.Peca;
import tabuleiro.Posicao;
import tabuleiro.Tabuleiro;

public abstract class PecaXadrez extends Peca {

	private Cor cor;
	private int contaMovimento;

	public PecaXadrez(Tabuleiro tabuleiro, Cor cor) {
		super(tabuleiro);
		this.cor = cor;
	}

	public Cor getCor() {
		return cor;
	}

	public int getContaMovimento() {
		return contaMovimento;
	}

	public void aumentaContagemMovimento() {
		contaMovimento++;
	}

	public void diminuiContagemMovimento() {
		contaMovimento--;
	}

	public PosicaoXadrez getPosicaoXadrez() {
		return PosicaoXadrez.paraPosicao(posicao);
	}

	protected boolean haPecaOponente(Posicao posicao) {
		PecaXadrez p = (PecaXadrez) getTabuleiro().peca(posicao);
		return p != null && p.getCor() != cor;
	}

}
