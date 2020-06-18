package xadrez;

import tabuleiro.Peca;
import tabuleiro.Posicao;
import tabuleiro.Tabuleiro;
import xadrez.pecas.Rei;
import xadrez.pecas.Torre;

public class PartidaXadrez {

	private int turno;
	private Cor jogadorAtual;
	private Tabuleiro tabuleiro;

	public PartidaXadrez() {
		tabuleiro = new Tabuleiro(8, 8);
		turno = 1;
		jogadorAtual = Cor.BRANCO;
		setupInicial();
	}

	public int getTurno() {
		return turno;
	}

	public Cor getJogadorAtual() {
		return jogadorAtual;
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

	public boolean[][] movimentosPossiveis(PosicaoXadrez fontePosicao) {
		Posicao posicao = fontePosicao.toPosicao();
		validaPosicaoFonte(posicao);
		return tabuleiro.peca(posicao).movimentosPossiveis();
	}

	public PecaXadrez performMovimentoXadrez(PosicaoXadrez fontePosicao, PosicaoXadrez destinoPosicao) {
		Posicao fonte = fontePosicao.toPosicao();
		Posicao destino = destinoPosicao.toPosicao();
		validaPosicaoFonte(fonte);
		validaPosicaoDestino(fonte, destino);
		Peca capturaPeca = realizaMovimento(fonte, destino);
		proximoTurno();
		return (PecaXadrez) capturaPeca;
	}

	private Peca realizaMovimento(Posicao origem, Posicao destino) {
		Peca p = tabuleiro.removePeca(origem);
		Peca pecaCapturada = tabuleiro.removePeca(destino);
		tabuleiro.coloquePeca(p, destino);
		return pecaCapturada;
	}

	private void validaPosicaoFonte(Posicao posicao) {
		if (!tabuleiro.haUmaPeca(posicao)) {
			throw new ChessException("Nao existe peca na posicao de origem");
		}
		if (jogadorAtual != ((PecaXadrez) tabuleiro.peca(posicao)).getCor()) {
			throw new ChessException("A peca escolhida nao e sua");
		}
		if (!tabuleiro.peca(posicao).existeAlgumMovimentoPossivel()) {
			throw new ChessException("Nao existe movimentos possiveis para a peca escolhida");
		}
	}

	private void validaPosicaoDestino(Posicao fonte, Posicao destino) {
		if (!tabuleiro.peca(fonte).movimentoPossivel(destino)) {
			throw new ChessException("A peca escolhida nao pode se mover para a posicao de destino");
		}
	}

	private void proximoTurno() {
		turno++;
		jogadorAtual = (jogadorAtual == Cor.BRANCO) ? Cor.PRETO : Cor.BRANCO;
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
