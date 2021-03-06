package com.nom.sudokuhint;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import com.nom.sudokuhint.Sudoku;

public class MainActivity extends Activity implements OnClickListener{

	Sudoku puzzle = new Sudoku();
	TableLayout table;
	static final int returndata = 1;
	static final int setdata = 2;
	ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
	HorizontalScrollView hScroll;
	ScrollView vScroll;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	   outState.putParcelable("update", puzzle);
	   super.onSaveInstanceState(outState);
	   //outState.putString("message", "This is my message to be reloaded");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		table = (TableLayout) findViewById(R.id.sudokuPuzzle);
		if(savedInstanceState != null) {
			puzzle = savedInstanceState.getParcelable("update");
		}
		String[][] data = puzzle.attemptGetter();
		int c = 0;
		for (int row = 0; row < 9; row++) {
			TableRow currentRow = new TableRow(this);
			for (int button = 0; button < 9; button++) {
				Button currentButton = new Button(this);
				currentButton.setId(c);
				currentButton.setText(data[row][button]);
				currentButton.setBackgroundResource(R.drawable.back);
				currentButton.setOnClickListener(this);
				currentRow.addView(currentButton);
				c++;
			}
			currentRow.setId(row);
			table.addView(currentRow);
		}
		hScroll = (HorizontalScrollView) findViewById(R.id.scrollHorizontal);
		vScroll = (ScrollView) findViewById(R.id.scrollVertical);
	}
	
	 @Override
	 public boolean onTouchEvent(MotionEvent event) {
	        float curX, curY, mx = 0 , my = 0;
	        

	        switch (event.getAction()) {

	            case MotionEvent.ACTION_DOWN:
	                mx = event.getX();
	                my = event.getY();
	                break;
	            case MotionEvent.ACTION_MOVE:
	                curX = event.getX();
	                curY = event.getY();
	                vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
	                hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
	                mx = curX;
	                my = curY;
	                break;
	            case MotionEvent.ACTION_UP:
	                curX = event.getX();
	                curY = event.getY();
	                vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
	                hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
	                break;
	        }

	        return true;
	    }

	@Override
	public void onClick(View v) {
		Intent sudoku_edit = new Intent(this, sudokuEdit.class);
		int buttonid = v.getId();
		String but = Integer.toString(buttonid);
		puzzle.stringify();
		String[] editList = puzzle.editListGetter();
		sudoku_edit.putExtra("data", editList);
		sudoku_edit.putExtra("pos", but);
		this.startActivityForResult(sudoku_edit, returndata);
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == returndata) {
			if (resultCode == RESULT_OK) {
				String newNum = data.getStringExtra("new");
				int pos = Integer.parseInt(data.getStringExtra("pos"));
				if (newNum.compareTo("Hint?") == 0) {
					newNum = puzzle.hintMatrix(pos);
				}
				Button btn = (Button)table.findViewById(pos);
				btn.setText(newNum);
				puzzle.setMatrix(pos, newNum);
			} 
		} else if (requestCode == setdata) {
			if (resultCode == RESULT_OK) {
				String[] newNums = data.getStringArrayExtra("newdata");
				puzzle.setMatrix(newNums);
				puzzle.stringify();
				setBtn(puzzle.editListGetter());
				String checkpossible = puzzle.attemptMatrix();
				if (checkpossible.compareTo("Sorry, this isn't right")== 0) {
					Toast.makeText(getApplicationContext(), checkpossible,
							Toast.LENGTH_SHORT).show();
					
					Intent sudoku_set = new Intent(this,sudokuSet.class);
					sudoku_set.putExtra("data", puzzle.editListGetter());
					this.startActivityForResult(sudoku_set, setdata);
				} else {
					Toast.makeText(getApplicationContext(), checkpossible,
							Toast.LENGTH_SHORT).show();
				}
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_restart:
			puzzle.restart();
			setBtn(puzzle.editListGetter());
			break;
		case R.id.menu_set:
			Intent sudoku_set = new Intent(this,sudokuSet.class);
			sudoku_set.putExtra("data", puzzle.editListGetter());
			this.startActivityForResult(sudoku_set,setdata);
			break;
		case R.id.menu_solve:
			puzzle.solutionMatrix();
			setBtn(puzzle.editListGetter());
			break;
		case R.id.menu_hint:
			int[] high = puzzle.hintMatrix();
			setBtn(puzzle.editListGetter());
			btnHighlight(high);
			break;
		case R.id.menu_check:
			String check = puzzle.checkMatrix();
			Toast.makeText(getApplicationContext(), check,
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_checkloc:
			int[] where = puzzle.checkWhere();
			btnHighlight(where);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	public void setBtn(String[] newdata) {
		table = (TableLayout) findViewById(R.id.sudokuPuzzle);
		for(int x=0;x<newdata.length;x++) {
			int i = x/9;
			int j = x%9;
			TableRow tr = (TableRow) table.getChildAt(i);
			Button btn = (Button) tr.getChildAt(j);
			btn.setText(newdata[x]);
		}
		puzzle.setMatrix(newdata);
	}
	
	public void btnHighlight(final int[] cells) {
		//Highlight button positions for 10 seconds
	table = (TableLayout) findViewById(R.id.sudokuPuzzle);
	for(int y=0;y<cells.length;y++) {
		int x = cells[y];
		int i = x/9;
		int j = x%9;
		TableRow tr = (TableRow) table.getChildAt(i);
		Button btn = (Button) tr.getChildAt(j);
		btn.setBackgroundResource(R.drawable.back2);
		}
	
	Runnable task = new Runnable() {
		@Override
		public void run() {
			Log.e("run", "this works?");
			runOnUiThread(new Runnable() {
				public void run() {
				refresh(cells);
				}
			});
			
		}
		
	};
	
	worker.schedule(task, 10, TimeUnit.SECONDS);
	
	}
	
	public void refresh(int[] cells) {
		for(int y=0;y<cells.length;y++) {
			int x = cells[y];
			int i = x/9;
			int j = x%9;
			TableRow tr = (TableRow) table.getChildAt(i);
			Button btn = (Button) tr.getChildAt(j);
			btn.setBackgroundResource(R.drawable.back);
			}
	}

}
