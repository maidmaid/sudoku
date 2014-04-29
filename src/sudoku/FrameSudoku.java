/***
 * Dany Maillard
 */
package sudoku;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sudoku.Sudoku.Algo;

public class FrameSudoku extends JFrame
{	
	private Sudoku sudoku = new Sudoku();
	private JPanel tools = new JPanel();
	private JSlider sliderHistorique = new JSlider();
	private JLabel labelHistoriqueEtape = new JLabel();
	private JLabel labelHistoriqueMax = new JLabel();
	
	public FrameSudoku()
	{
		super();
		initGUI();

		SudokuListener sl = new SudokuListener()
		{
			public void isComplet(Object o)
			{
				setTitle(sudoku.statistique());
				sliderHistorique.setValue(sudoku.historiqueMax());
			}

			public void erreur(Object o)
			{
				setTitle(sudoku.statistique());
				sliderHistorique.setValue(sudoku.historiqueMax());
			}

			public void historiqueChange(Object o)
			{
				int etape = sudoku.historiqueEtapeAcutelle();
				sliderHistorique.setValue(etape);
			}
		};
		
		sudoku.addListener(sl);
		
		//sudoku.generer(Algo.incrementalAvecCasePrioritaireAvecPremiereValeur);
		//sudoku.generer(Algo.aleatoireAvecCasePrioritaireAvecValeurIdeale);
		//Timer t = sudoku.genererTimer(Algo.aleatoireAvecCasePrioritaireAvecValeurIdeale, 1);
		genererSudoku(1000);
	}
	
	public static void main(String[] args)
	{
		FrameSudoku s = new FrameSudoku();
		s.setVisible(true);
	}
	
	private void initGUI()
	{
		setSize(1000, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridLayout(1, 2));
		
		sliderHistorique.addMouseListener(new MouseListener()
		{
			public void mouseReleased(MouseEvent arg0)
			{
				int etape = sliderHistorique.getValue();
				sudoku.setHistorique(etape);
			}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent arg0) {}
		});
		sliderHistorique.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent arg0)
			{
				int etape = sliderHistorique.getValue();
				int max = sudoku.historiqueMax();
				labelHistoriqueEtape.setText("Etape " + etape);
				labelHistoriqueMax.setText("Etape maximale " + max);
				//sudoku.setHistorique(etape);
			}
		});
		sliderHistorique.setMinimum(1);
		sliderHistorique.setMaximum(81);
		sliderHistorique.setMajorTickSpacing(10);
		sliderHistorique.setMinorTickSpacing(1);
		sliderHistorique.setPaintTicks(true);
		sliderHistorique.setPaintLabels(true);
		
		JButton btGenerer = new JButton("Générer une grille");
		btGenerer.addMouseListener(new MouseListener()
		{	
			public void mouseReleased(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent arg0)
			{
				sudoku.genererTimer(Algo.aleatoireAvecCasePrioritaireAvecValeurIdeale, 100);
			}
		});
		
		JButton btReset = new JButton("Reset");
		btReset.addMouseListener(new MouseListener()
		{	
			public void mouseReleased(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent arg0)
			{
				sudoku.reset();
			}
		});
		
		
		tools.setLayout(new GridLayout(5, 1));
		tools.add(labelHistoriqueEtape);
		tools.add(labelHistoriqueMax);
		tools.add(sliderHistorique);		
		tools.add(btGenerer);
		tools.add(btReset);
		
		add(sudoku);
		add(tools);
	}
	
	public void genererSudoku(int nombre)
	{		
		for (int i = 0; i < nombre; i++)
		{
			sudoku.reset();
			sudoku.generer(Algo.aleatoireAvecCasePrioritaireAvecValeurIdeale);
			System.out.println(sudoku.statistique());
		}
	}
}