/***
 * Dany Maillard
 */
package sudoku;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.print.attribute.standard.Finishings;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Sudoku extends JPanel
{
	private ArrayList<SudokuListener> listeners = new ArrayList<SudokuListener>();
	private Case[][] cases = new Case[9][9];
	private ArrayList<Case> casesPrioritaires = new ArrayList<>();
	private ArrayList<Case> casesRestantes = new ArrayList<>();
	private int nombreCasesAssignees = 0;
	private int totalComplet = 0;
	private int totalErreur = 0;
	private boolean stopped = false;
	private long tStart = 0;
	private long tEnd = 0;
	private ArrayList<Case> historique = new ArrayList<>();
	private int historiqueEtapeActuelle = 0;
	
	public enum Algo
	{
		incrementalAvecCasePrioritaireAvecPremiereValeur,
		aleatoireAvecCasePrioritaireAvecPremiereValeur,
		aleatoireAvecCasePrioritaireAvecValeurIdeale
	}
	
	/***
	 * La classe Sudoku permet de générer une grille de sudoku.
	 */
	public Sudoku()
	{
		initGUI();
		reset();
	}
	
	/***
	 * Supprime un écouteur de sudoku
	 * @param listener écouteur de sudoku
	 */
	public void removeListener(SudokuListener listener)
	{
		listeners.remove(listener);
	}

	public void addListener(SudokuListener listener)
	{
		listeners.add(listener);
	}
	
	public void reset()
	{
		stopped = false;
		nombreCasesAssignees = 0;
		casesRestantes.clear();
		for (int i = 0; i < 9; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				cases[i][j].reset();
				casesRestantes.add(cases[i][j]);
			}
		}
		historique.clear();
		historiqueEtapeActuelle = 0;
	}
	
	private void initGUI()
	{		
		GridLayout layout = new GridLayout(9, 9);
		setLayout(layout);
		
		for (int i = 0; i < 9; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				Case c = new Case(i + 1, j + 1);
				
				CaseListener l = new CaseListener()
				{	
					public void valeurAssignee(Object o)
					{
						nombreCasesAssignees++;
						Case c = (Case)o;
						
						int x = c.x();
						int y = c.y();
						int valeur = c.valeur();
						
						casesRestantes.remove(c);
						
						if(!c.isHistorique())
							addHistorique(c);
						
						setValeur(x, y, valeur, false);
						casesPrioritaires();
						
						if(isComplet())
							isCompletSignal();
					}
				};
				
				c.addListener(l);
				
				cases[i][j] = c;
				add(c);
			}
		}
	}
	
	private Case getCase(int x, int y)
	{
		Case c = cases[x - 1][y - 1];
		return c;
	}
	
	public void assignerValeur(int x, int y, int valeur)
	{
		Case c = getCase(x, y);
		c.setValeur(valeur);
	}
	
	private ArrayList<Case> supprimerPossibiliteParLigne(int ligne, int valeur, boolean simuler)
	{
		ArrayList<Case> casesTouchees = new ArrayList<>();
		
		for (int i = 1; i <= 9; i++)
		{
			Case c = getCase(ligne, i);
			if(!c.isValeurAssignee() && c.isPossible(valeur))
			{
				if(!simuler)
					c.supprimerPossibilite(valeur);
				
				casesTouchees.add(c);
			}
		}
		
		return casesTouchees;
	}
	
	private ArrayList<Case> supprimerPossibiliteParColonne(int colonne, int valeur, boolean simuler)
	{
		ArrayList<Case> casesTouches = new ArrayList<>();
		
		for (int i = 1; i <= 9; i++)
		{
			Case c = getCase(i, colonne);
			if(!c.isValeurAssignee() && c.isPossible(valeur))
			{
				if(!simuler)
					c.supprimerPossibilite(valeur);
				
				casesTouches.add(c);
			}
		}
		
		return casesTouches;
	}
	
	private ArrayList<Case> supprimerPossibiliteParRegion(int x, int y, int valeur, boolean simuler)
	{
		ArrayList<Case> casesTouchees = new ArrayList<>();
		int iStart = getDebutRegion(x);
		int jStart = getDebutRegion(y);
		
		for (int i = iStart; i < iStart + 3; i++)
		{
			for (int j = jStart; j < jStart + 3; j++)
			{
				Case c = getCase(i, j);
				if(!c.isValeurAssignee() && c.isPossible(valeur))
				{
					if(!simuler)
						c.supprimerPossibilite(valeur);
					
					casesTouchees.add(c);
				}
			}
		}
		
		return casesTouchees;
	}
	
	private int getDebutRegion(int region)
	{
		int debutRegion = 0;
		
		switch (region)
		{
		case 1:
		case 2:
		case 3:
			debutRegion = 1;
			break;
		case 4:
		case 5:
		case 6:
			debutRegion = 4;
			break;
		case 7:
		case 8:
		case 9:
			debutRegion = 7;
			break;
		default:
			break;
		}
		
		return debutRegion;
	}
	
	public ArrayList<Case> setValeur(int x, int y, int valeur, boolean simuler)
	{
		ArrayList<Case> casesTouchees = new ArrayList<>();
		
		casesTouchees.addAll(supprimerPossibiliteParLigne(x, valeur, simuler));
		casesTouchees.addAll(supprimerPossibiliteParColonne(y, valeur, simuler));
		casesTouchees.addAll(supprimerPossibiliteParRegion(x, y, valeur, simuler));
		
		//Sans doublons
		Set<Case> sansDoublons = new HashSet<>(casesTouchees);
		casesTouchees = new ArrayList<>(sansDoublons);
		
		return casesTouchees;
	}
	
	private ArrayList<Case> setValeur(Case c, int valeur, boolean simuler)
	{
		return setValeur(c.x(), c.y(), valeur, simuler);
	}
	
	private void casesPrioritaires()
	{
		int minPossibilites = 9;
		int nombrePossibilites = 0;
		ArrayList<Case> casesPrioritaires = new ArrayList<>();
		
		for (int i = 1; i <= 9; i++)
		{
			for (int j = 1; j <= 9; j++)
			{
				Case c = getCase(i, j);
				
				if(!c.isValeurAssignee())
				{
					c.setPrioritaire(false);
					nombrePossibilites = c.nombrePossibilites();
					
					// Case prioriataire ajoutée à la liste
					if(nombrePossibilites == minPossibilites)
					{
						casesPrioritaires.add(c);
					}
					
					// Nouvelle priorité
					if(nombrePossibilites < minPossibilites && nombrePossibilites != 0)
					{
						minPossibilites = c.nombrePossibilites();
						casesPrioritaires.clear();
						casesPrioritaires.add(c);
					}
					
					// Case n'ayant plus de possibilité ni de valeur affectée : erreur
					if(nombrePossibilites == 0)
					{
						erreurSignal();
					}	
				}
			}
		}
		
		for (Case c : casesPrioritaires)
		{
			c.setPrioritaire(true);
			setCasesTouchees(c);
		}
		
		this.casesPrioritaires = casesPrioritaires;
	}
	
	private void erreurSignal()
	{	
		stopped = true;
		totalErreur++;
		
		for (SudokuListener sl : listeners)
		{
			sl.erreur(this);
		}
	}

	public String statistique()
	{
		double p = pourcentageReussite();
		double t = tempsExecution();
		String stat = "|Grille générée en " + t + " ms| |Erreur: " + totalErreur + "| |Complet: " + totalComplet + "| |" + p + "% de réussite|";
		return stat;
	}
	
	public boolean isFinish()
	{
		boolean finish = false;
		finish = (nombreCasesAssignees == 81 || stopped);
		return finish;
	}
	
	public boolean isComplet()
	{
		boolean isComplet = false;
		isComplet = (nombreCasesAssignees == 81);
		return isComplet;
	}
	
	private void isCompletSignal()
	{
		totalComplet++;
		for (SudokuListener sl : listeners)
		{
			sl.isComplet(this);
		}
	}
	
	/***
	 * Correspond à l'algo Algo.aleatoireAvecCasePrioritaireAvecPremiereValeur
	 */
	private void algo2()
	{
		if(!isFinish())
		{
			casesPrioritaires();
			
			if(casesPrioritaires.size() == 0)
			{
				erreurSignal();
			}
			else
			{
				Random random = new Random();
				int index = 0;
				index = Math.abs(random.nextInt() % casesPrioritaires.size());
				Case c = casesPrioritaires.get(index);
				c.setPremiereValeur();
			}
		}
	}
	
	/***
	 * Correspond à l'algo Algo.incrementalAvecCasePrioritaireAvecPremiereValeur
	 */
	private void algo1()
	{
		if(!isFinish())
		{
			casesPrioritaires();
			Case c = casesPrioritaires.get(0);
			c.setPremiereValeur();
		}
	}
	
	/***
	 * Correspond à l'algo Algo.aleatoireAvecCasePrioritaireAvecValeurIdeale
	 */
	private void algo3()
	{
		if(!isFinish())
		{
			casesPrioritaires();
			
			if(casesPrioritaires.size() == 0)
			{
				erreurSignal();
			}
			else
			{
			
				Random random = new Random();
				int index = 0;
				index = Math.abs(random.nextInt() % casesPrioritaires.size());				
				Case c = casesPrioritaires.get(index);
				c.setValeurIdeale();
			}
		}
	}
	
	public void generer(Algo algo)
	{
		start();
		while(!isFinish())
		{
			switch (algo)
			{
				case incrementalAvecCasePrioritaireAvecPremiereValeur:
					algo1();
					break;
				case aleatoireAvecCasePrioritaireAvecPremiereValeur:
					algo2();
					break;
				case aleatoireAvecCasePrioritaireAvecValeurIdeale:
					algo3();
					break;
			}
		}
		stop();
	}
	
	public Timer genererTimer(final Algo algo, int speed)
	{
		final Timer timer = new Timer(speed, null);
		
		ActionListener action = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				switch (algo)
				{
					case incrementalAvecCasePrioritaireAvecPremiereValeur:
						algo1();
						break;
					case aleatoireAvecCasePrioritaireAvecPremiereValeur:
						algo2();
						break;
					case aleatoireAvecCasePrioritaireAvecValeurIdeale:
						algo3();
						break;
				}
				
				if(isFinish())
					timer.stop();
			}
		};
		
		timer.addActionListener(action);
		timer.start();
		
		return timer;
	}
	
	private void setCasesTouchees(Case c)
	{
		int totalSuppression = 0;
		
		if(!c.isValeurAssignee())
		{
			for (int i = 1; i <= 9; i++)
			{
				if(c.isPossible(i))
				{
					totalSuppression = setValeur(c, i, true).size();
					c.setCasesTouchees(i, totalSuppression);
				}
			}
			
			c.colorierPossibilitesIdeales();
		}
	}
	
	private void start()
	{
		tStart = System.currentTimeMillis();
	}
	
	private void stop()
	{
		tEnd = System.currentTimeMillis();
	}
	
	public double tempsExecution()
	{
		double sec = 0;
		sec = (double) (tEnd - tStart);
		return sec;
	}
	
	public double pourcentageReussite()
	{
		double pourcentageReussite = 0;
		pourcentageReussite = (1.0 - ((double)totalErreur)/((double)(totalComplet + totalErreur))) * 100.0;
		return pourcentageReussite;
	}
	
	public int historiqueMax()
	{
		int historiqueMax = 0;
		int size = historique.size();
		return size;
	}
	
	/***
	 * Accède à l'historique de la création du Sudoku
	 * @param etape Etape de l'historique
	 */
	public void setHistorique(int etape)
	{
		historiqueEtapeActuelle = etape;
		int x = 0;
		int y = 0;
		int valeur = 0;
		
		// Reset en gardant l'historique
		ArrayList<Case> h = (ArrayList<Case>)this.historique.clone();
		reset();
		this.historique = h;
		
		for (int i = 1; i <= etape; i++)
		{
			Case c = historique.get(i - 1);
			x = c.x();
			y = c.y();
			valeur = c.valeur();
			
			cases[x - 1][y - 1].setValeur(valeur);
		}
	}
	
	public int historiqueEtapeAcutelle()
	{
		return historiqueEtapeActuelle;
	}
	
	private void addHistorique(Case c)
	{
		c.setHistorique(true);
		Case cClone = (Case)c.clone();
		historique.add(cClone);
		historiqueEtapeActuelle++;
		historiqueChangeSignal();
	}
	
	private void historiqueChangeSignal()
	{
		for (SudokuListener sl : listeners)
		{
			sl.historiqueChange(this);
		}
	}
}
