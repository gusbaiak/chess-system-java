package xadrez;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import tabuleiro.Peca;
import tabuleiro.Posicao;
import tabuleiro.Tabuleiro;
import xadrez.pecas.Bispo;
import xadrez.pecas.Cavalo;
import xadrez.pecas.Peao;
import xadrez.pecas.Rainha;
import xadrez.pecas.Rei;
import xadrez.pecas.Torre;

public class PartidaXadrez {

	private int turno;
	private Cor jogadorAtual;
	private Tabuleiro tabuleiro;
	private boolean xeque;
	private boolean xequeMate;
	private PecaXadrez enPassantVuneravel;
	private PecaXadrez promovido;

	private List<Peca> pecasNoTabuleiro = new ArrayList<>();
	private List<Peca> pecasCapturadas = new ArrayList<>();

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

	public boolean getXeque() {
		return xeque;
	}

	public boolean getXequeMate() {
		return xequeMate;
	}

	public PecaXadrez getEnPassantVuneravel() {
		return enPassantVuneravel;
	}

	public PecaXadrez getPromovido() {
		return promovido;
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

		if (testaXeque(jogadorAtual)) {
			desfazMovimento(fonte, destino, capturaPeca);
			throw new ChessException("Voce nao pode se colocar em xeque");
		}

		PecaXadrez pecaMovida = (PecaXadrez) tabuleiro.peca(destino);

		// #MovimentoEspecial promoção
		promovido = null;
		if (pecaMovida instanceof Peao) {
			if (pecaMovida.getCor() == Cor.BRANCO && destino.getLinha() == 0
					|| pecaMovida.getCor() == Cor.PRETO && destino.getLinha() == 7) {
				promovido = (PecaXadrez) tabuleiro.peca(destino);
				promovido = recolocaPecaPromovida("Q");
			}
		}

		xeque = (testaXeque(oponente(jogadorAtual))) ? true : false;

		if (testaXequeMate(oponente(jogadorAtual))) {
			xequeMate = true;
		} else {
			proximoTurno();
		}

		// #MovimentoEpecial en Passant
		if (pecaMovida instanceof Peao && (destino.getLinha() == fonte.getLinha() - 2)
				|| (destino.getLinha() == fonte.getLinha() + 2)) {
			enPassantVuneravel = pecaMovida;
		} else {
			enPassantVuneravel = null;
		}
		return (PecaXadrez) capturaPeca;
	}

	public PecaXadrez recolocaPecaPromovida(String tipo) {
		if (promovido == null) {
			throw new IllegalStateException("Nao ha peca para ser promovida");
		}
		if (!tipo.equals("B") && !tipo.equals("C") && !tipo.equals("T") && !tipo.equals("Q")) {
			throw new InvalidParameterException("Tipo invalido para a promocao");
		}

		Posicao pos = promovido.getPosicaoXadrez().toPosicao();
		Peca p = tabuleiro.removePeca(pos);
		pecasNoTabuleiro.remove(p);

		PecaXadrez novaPeca = novaPeca(tipo, promovido.getCor());
		tabuleiro.coloquePeca(novaPeca, pos);
		pecasNoTabuleiro.add(novaPeca);

		return novaPeca;
	}

	private PecaXadrez novaPeca(String tipo, Cor cor) {
		if (tipo.equals("B"))
			return new Bispo(tabuleiro, cor);
		if (tipo.equals("C"))
			return new Cavalo(tabuleiro, cor);
		if (tipo.equals("T"))
			return new Torre(tabuleiro, cor);
		return new Rainha(tabuleiro, cor);
	}

	private Peca realizaMovimento(Posicao origem, Posicao destino) {
		PecaXadrez p = (PecaXadrez) tabuleiro.removePeca(origem);
		p.aumentaContagemMovimento();
		Peca pecaCapturada = tabuleiro.removePeca(destino);
		tabuleiro.coloquePeca(p, destino);

		if (pecaCapturada != null) {
			pecasNoTabuleiro.remove(pecaCapturada);
			pecasCapturadas.add(pecaCapturada);
		}

		// #MovimentoEspecial Roque do lado do rei
		if (p instanceof Rei && destino.getColuna() == origem.getColuna() + 2) {
			Posicao origemT = new Posicao(origem.getLinha(), origem.getColuna() + 3);
			Posicao destinoT = new Posicao(origem.getLinha(), origem.getColuna() + 1);
			PecaXadrez torre = (PecaXadrez) tabuleiro.removePeca(origemT);
			tabuleiro.coloquePeca(torre, destinoT);
			torre.aumentaContagemMovimento();
		}

		// #MovimentoEspecial Roque do lado da Rainh
		if (p instanceof Rei && destino.getColuna() == origem.getColuna() - 2) {
			Posicao origemT = new Posicao(origem.getLinha(), origem.getColuna() - 4);
			Posicao destinoT = new Posicao(origem.getLinha(), origem.getColuna() - 1);
			PecaXadrez torre = (PecaXadrez) tabuleiro.removePeca(origemT);
			tabuleiro.coloquePeca(torre, destinoT);
			torre.aumentaContagemMovimento();
		}

		// #MovimentoEspecial en passant
		if (p instanceof Peao) {
			if (origem.getColuna() != destino.getColuna() && pecaCapturada == null) {
				Posicao posicaoPeao;
				if (p.getCor() == Cor.BRANCO) {
					posicaoPeao = new Posicao(destino.getLinha() + 1, destino.getColuna());
				} else {
					posicaoPeao = new Posicao(destino.getLinha() - 1, destino.getColuna());
				}
				pecaCapturada = tabuleiro.removePeca(posicaoPeao);
				pecasCapturadas.add(pecaCapturada);
				pecasNoTabuleiro.remove(pecaCapturada);
			}
		}

		return pecaCapturada;
	}

	private void desfazMovimento(Posicao fonte, Posicao destino, Peca pecaCapturada) {
		PecaXadrez p = (PecaXadrez) tabuleiro.removePeca(destino);
		p.diminuiContagemMovimento();
		tabuleiro.coloquePeca(p, fonte);

		if (pecaCapturada != null) {
			tabuleiro.coloquePeca(pecaCapturada, destino);
			pecasCapturadas.remove(pecaCapturada);
			pecasNoTabuleiro.add(pecaCapturada);
		}

		// #MovimentoEspecial Roque do lado do rei
		if (p instanceof Rei && destino.getColuna() == fonte.getColuna() + 2) {
			Posicao fonteT = new Posicao(fonte.getLinha(), fonte.getColuna() + 3);
			Posicao destinoT = new Posicao(fonte.getLinha(), fonte.getColuna() + 1);
			PecaXadrez torre = (PecaXadrez) tabuleiro.removePeca(destinoT);
			tabuleiro.coloquePeca(torre, fonteT);
			torre.aumentaContagemMovimento();
		}

		// #MovimentoEspecial Roque do lado da Rainh
		if (p instanceof Rei && destino.getColuna() == fonte.getColuna() - 2) {
			Posicao fonteT = new Posicao(fonte.getLinha(), fonte.getColuna() - 4);
			Posicao destinoT = new Posicao(fonte.getLinha(), fonte.getColuna() - 1);
			PecaXadrez torre = (PecaXadrez) tabuleiro.removePeca(destinoT);
			tabuleiro.coloquePeca(torre, fonteT);
			torre.aumentaContagemMovimento();
		}

		// #MovimentoEspecial en passant
		if (p instanceof Peao) {
			if (fonte.getColuna() != destino.getColuna() && pecaCapturada == enPassantVuneravel) {
				PecaXadrez peao = (PecaXadrez) tabuleiro.removePeca(destino);
				Posicao posicaoPeao;
				if (p.getCor() == Cor.BRANCO) {
					posicaoPeao = new Posicao(3, destino.getColuna());
				} else {
					posicaoPeao = new Posicao(4, destino.getColuna());
				}
				tabuleiro.coloquePeca(peao, posicaoPeao);
			}
		}
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

	private Cor oponente(Cor cor) {
		return (cor == Cor.BRANCO) ? Cor.PRETO : Cor.BRANCO;
	}

	private PecaXadrez rei(Cor cor) {
		List<Peca> lista = pecasNoTabuleiro.stream().filter(x -> ((PecaXadrez) x).getCor() == cor)
				.collect(Collectors.toList());
		for (Peca peca : lista) {
			if (peca instanceof Rei) {
				return (PecaXadrez) peca;
			}
		}
		throw new IllegalStateException("Nao existe o rei " + cor + " no tabuleiro");
	}

	private boolean testaXeque(Cor cor) {
		Posicao posicaoRei = rei(cor).getPosicaoXadrez().toPosicao();
		List<Peca> pecasOponente = pecasNoTabuleiro.stream().filter(x -> ((PecaXadrez) x).getCor() == oponente(cor))
				.collect(Collectors.toList());
		for (Peca peca : pecasOponente) {
			boolean[][] mat = peca.movimentosPossiveis();
			if (mat[posicaoRei.getLinha()][posicaoRei.getColuna()]) {
				return true;
			}
		}
		return false;
	}

	private boolean testaXequeMate(Cor cor) {
		if (!testaXeque(cor)) {
			return false;
		}
		List<Peca> lista = pecasNoTabuleiro.stream().filter(x -> ((PecaXadrez) x).getCor() == cor)
				.collect(Collectors.toList());
		for (Peca peca : lista) {
			boolean[][] mat = peca.movimentosPossiveis();
			for (int i = 0; i < tabuleiro.getLinhas(); i++) {
				for (int j = 0; j < tabuleiro.getColunas(); j++) {
					if (mat[i][j]) {
						Posicao fonte = ((PecaXadrez) peca).getPosicaoXadrez().toPosicao();
						Posicao destino = new Posicao(i, j);
						Peca pecaCapturada = realizaMovimento(fonte, destino);
						boolean testaXeque = testaXeque(cor);
						desfazMovimento(fonte, destino, pecaCapturada);
						if (!testaXeque) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private void coloqueNovaPeca(char coluna, int linha, PecaXadrez pecaXadrez) {
		tabuleiro.coloquePeca(pecaXadrez, new PosicaoXadrez(coluna, linha).toPosicao());
		pecasNoTabuleiro.add(pecaXadrez);
	}

	private void setupInicial() {
		coloqueNovaPeca('a', 1, new Torre(tabuleiro, Cor.BRANCO));
		coloqueNovaPeca('h', 1, new Torre(tabuleiro, Cor.BRANCO));
		coloqueNovaPeca('e', 1, new Rei(tabuleiro, Cor.BRANCO, this));
		coloqueNovaPeca('a', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		coloqueNovaPeca('b', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		coloqueNovaPeca('c', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		coloqueNovaPeca('d', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		coloqueNovaPeca('e', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		coloqueNovaPeca('f', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		coloqueNovaPeca('g', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		coloqueNovaPeca('h', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		coloqueNovaPeca('c', 1, new Bispo(tabuleiro, Cor.BRANCO));
		coloqueNovaPeca('f', 1, new Bispo(tabuleiro, Cor.BRANCO));
		coloqueNovaPeca('b', 1, new Cavalo(tabuleiro, Cor.BRANCO));
		coloqueNovaPeca('g', 1, new Cavalo(tabuleiro, Cor.BRANCO));
		coloqueNovaPeca('d', 1, new Rainha(tabuleiro, Cor.BRANCO));

		coloqueNovaPeca('a', 8, new Torre(tabuleiro, Cor.PRETO));
		coloqueNovaPeca('h', 8, new Torre(tabuleiro, Cor.PRETO));
		coloqueNovaPeca('e', 8, new Rei(tabuleiro, Cor.PRETO, this));
		coloqueNovaPeca('a', 7, new Peao(tabuleiro, Cor.PRETO, this));
		coloqueNovaPeca('b', 7, new Peao(tabuleiro, Cor.PRETO, this));
		coloqueNovaPeca('c', 7, new Peao(tabuleiro, Cor.PRETO, this));
		coloqueNovaPeca('d', 7, new Peao(tabuleiro, Cor.PRETO, this));
		coloqueNovaPeca('e', 7, new Peao(tabuleiro, Cor.PRETO, this));
		coloqueNovaPeca('f', 7, new Peao(tabuleiro, Cor.PRETO, this));
		coloqueNovaPeca('g', 7, new Peao(tabuleiro, Cor.PRETO, this));
		coloqueNovaPeca('h', 7, new Peao(tabuleiro, Cor.PRETO, this));
		coloqueNovaPeca('c', 8, new Bispo(tabuleiro, Cor.PRETO));
		coloqueNovaPeca('f', 8, new Bispo(tabuleiro, Cor.PRETO));
		coloqueNovaPeca('b', 8, new Cavalo(tabuleiro, Cor.PRETO));
		coloqueNovaPeca('g', 8, new Cavalo(tabuleiro, Cor.PRETO));
		coloqueNovaPeca('d', 8, new Rainha(tabuleiro, Cor.PRETO));

	}

}
