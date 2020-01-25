package com.app.frank;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

public class FrankGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture background;
	Texture[] bird;
	int birdStateFlag = 0;
	float flyHeight;
	float fallingSpeed;
	int gameStateFlag = 0;

	Texture topTube;
	Texture bottomTube;
	float spaceBetweenTubes = 550;
	Random random;

	int tubeSpeed = 5;

	int tubesNumber = 5;
	float distanceBetweenTubes;
	float tubeX[] = new float[tubesNumber];
	float tubeShift[] = new float[tubesNumber];

	Circle birdCircle;
	Rectangle[] topTubeRectanfles;
	Rectangle[] bottomTubeRectanfles;
	//ShapeRenderer shapeRenderer;

	int gameScore = 0;
	int passedTubeIndex;
	BitmapFont scoreFont;

	Texture gameOver;

	@Override
	public void create () {
		batch = new SpriteBatch();

		birdCircle = new Circle();
		topTubeRectanfles = new Rectangle[tubesNumber];
		bottomTubeRectanfles = new Rectangle[tubesNumber];

		background = new Texture("background.png");
		topTube = new Texture("top_tube.png");
		bottomTube = new Texture("bottom_tube.png");
		gameOver = new Texture("game_over.png");
		bird = new Texture[2];
		bird[0] = new Texture("bird_wings_up.png");
		bird[1] = new Texture("bird_wings_down.png");


		random = new Random();
		scoreFont = new BitmapFont();
		scoreFont.setColor(Color.WHITE);
		scoreFont.getData().setScale(10);
		scoreFont.setFixedWidthGlyphs("0123456789");


		distanceBetweenTubes = Gdx.graphics.getWidth() / 2;
		initGame();
	}

	public void initGame(){
		flyHeight = Gdx.graphics.getHeight() / 2 - bird[birdStateFlag].getHeight() / 2;
		for(int i = 0; i < tubesNumber; i++){
			tubeX[i] = Gdx.graphics.getWidth() / 2 - topTube.getWidth() / 2 + Gdx.graphics.getWidth() + i * distanceBetweenTubes *1.25f;
			tubeShift[i] = (random.nextFloat() -0.5F) * (Gdx.graphics.getHeight() - spaceBetweenTubes - 200);
			topTubeRectanfles[i] = new Rectangle();
			bottomTubeRectanfles[i] = new Rectangle();
		}
	}

	@Override
	public void render () {
		batch.begin();
		batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if(gameStateFlag == 1){

			Gdx.app.log("Game score", String.valueOf(gameScore));

			if(tubeX[passedTubeIndex] < Gdx.graphics.getWidth() / 2){
				gameScore ++;
				if(passedTubeIndex < tubesNumber -1){
					passedTubeIndex ++;
				} else {
					passedTubeIndex = 0;
				}
			}


			if(Gdx.input.justTouched()){
				fallingSpeed = -25;
			}

			for(int i = 0; i < tubesNumber; i++) {

				if(tubeX[i] < -topTube.getWidth()){
					tubeX[i] =tubesNumber * distanceBetweenTubes;
				} else {
					tubeX[i] -= tubeSpeed;
				}
				batch.draw(topTube, tubeX[i],
						Gdx.graphics.getHeight() / 2 + spaceBetweenTubes / 2 + tubeShift[i]);
				batch.draw(bottomTube, tubeX[i],
						Gdx.graphics.getHeight() / 2 - spaceBetweenTubes / 2 - bottomTube.getHeight() + tubeShift[i]);

				topTubeRectanfles[i] = new Rectangle(tubeX[i],
						Gdx.graphics.getHeight() / 2 + spaceBetweenTubes / 2 + tubeShift[i],
						topTube.getWidth(), topTube.getHeight());
				bottomTubeRectanfles[i] = new Rectangle(tubeX[i],
						Gdx.graphics.getHeight() / 2 - spaceBetweenTubes / 2 - bottomTube.getHeight() + tubeShift[i],
						bottomTube.getWidth(), bottomTube.getHeight());
			}

			if(flyHeight > 0 ){
				fallingSpeed++;
				flyHeight -= fallingSpeed;
			} else {
				gameStateFlag = 2;
			}
		} else if (gameStateFlag == 0){
			if(Gdx.input.justTouched()){
				gameStateFlag = 1;
			}
		} else if (gameStateFlag == 2){
			batch.draw(gameOver, Gdx.graphics.getWidth() / 2 - gameOver.getWidth() / 2,
					Gdx.graphics.getHeight() / 2 - gameOver.getHeight() / 2);
			if(Gdx.input.justTouched()){
				gameStateFlag = 1;
				initGame();
				gameScore = 0;
				passedTubeIndex = 0;
				fallingSpeed = 0;
			}
		}




		if(birdStateFlag == 0){
			birdStateFlag = 1;
		} else {
			birdStateFlag = 0;
		}

		batch.draw(bird[birdStateFlag],Gdx.graphics.getWidth() / 2 - bird[birdStateFlag].getWidth() / 2
				, flyHeight);
		scoreFont.draw(batch, String.valueOf(gameScore),
				Gdx.graphics.getWidth()/2 - 40, Gdx.graphics.getHeight() - 150);
		batch.end();

		birdCircle.set(Gdx.graphics.getWidth() / 2,
				flyHeight + bird[birdStateFlag].getHeight() / 2, bird[birdStateFlag].getWidth()/2);

		for(int i = 0; i < tubesNumber; i++) {

			if(Intersector.overlaps(birdCircle, topTubeRectanfles[i])
					|| Intersector.overlaps(birdCircle, bottomTubeRectanfles[i])){
				Gdx.app.log("Intersected", "Bump");
				gameStateFlag = 2;
			}
		}

	}

}
