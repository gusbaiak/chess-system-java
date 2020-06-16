package xadrez;

import tabuleiro.Tabuleiro;
import xadrez.pecas.Rei;
import xadrez.pecas.Torre;

public class PartidaXadrez {

	private Tabuleiro tabuleiro;

	public PartidaXadrez() {
		tabuleiro = new Tabuleiro(8, 8);
		setupInicial();
	}

	public PecaXadrez[][] getPecas() {
		PecaXadrez[][] mat = new PecaXadrez[tabuleiro.getLinhas()][tabuleiro.getColunas()];
		for (int i = 0; i < tabuleiro.getLinhas(); i++) {
			for (int j = 0; j < tabuleiro.getColunas(); j++) {
				mat[i][j] = (PecaXadrez) tabuleiro.peca(i, j);
			}
		}

		return mat;
	}

	private void coloqueNovaPeca(char coluna, int linha, PecaXadrez pecaXadrez) {
		tabuleiro.coloquePeca(pecaXadrez, new PosicaoXadrez(coluna, linha).toPosicao());
	}

	private void setupInicial() {
		coloqueNovaPeca('c', 1, new Torre(tabuleiro, Cor.BRANCO));
		coloqueNovaPeca('c', 2, new Torre(tabuleiro, Cor.BRANCO));
		coloqueNovaPeca('d', 2, new Torre(tabuleiro, Cor.BRANCO));
		coloqueNovaPeca('e', 2, new Torre(tabuleiro, Cor.BRANCO));
		coloqueNovaPeca('e', 1, new Torre(tabuleiro, Cor.BRANCO));
		coloqueNovaPeca('d', 1, new Rei(tabuleiro, Cor.BRANCO));

		coloqueNovaPeca('c', 7, new Torre(tabuleiro, Cor.PRETO));
		coloqueNovaPeca('c', 8, new Torre(tabuleiro, Cor.PRETO));
		coloqueNovaPeca('d', 7, new Torre(tabuleiro, Cor.PRETO));
		coloqueNovaPeca('e', 7, new Torre(tabuleiro, Cor.PRETO));
		coloqueNovaPeca('e', 8, new Torre(tabuleiro, Cor.PRETO));
		coloqueNovaPeca('d', 8, new Rei(tabuleiro, Cor.PRETO));
	}

}
