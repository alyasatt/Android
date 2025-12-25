package com.example.my_pet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.view.View;
import android.widget.LinearLayout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Creature creature;
    private ImageView imageView;
    private TextView hungerText;
    private TextView tirednessText;
    private TextView boredomText;
    private TextView happinessText;
    private TextView timeText;
    private LinearLayout gameLayout;
    private LinearLayout deathScreenLayout;
    private ImageView deadImage;
    private TextView deathMessageText;
    private Button restartButton;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int gameSpeed = 2000; // 2 секунды
    private long startTime;
    private long bestTime;
    
    // Runnable для возврата к нормальному состоянию после действия
    private Runnable returnToNormalRunnable;

    private final Runnable gameLoop = new Runnable() {
        @Override
        public void run() {
            creature.timePass();
            updateUI();

            if (creature.isAlive()) {
                gameSpeed = Math.max(500, gameSpeed - 10); // ускорение
                handler.postDelayed(this, gameSpeed);
            } else {
                showDeathScreen();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация игровых элементов
        imageView = findViewById(R.id.creatureImage);
        hungerText = findViewById(R.id.hungerText);
        tirednessText = findViewById(R.id.tirednessText);
        boredomText = findViewById(R.id.boredomText);
        happinessText = findViewById(R.id.happinessText);
        timeText = findViewById(R.id.timeText);
        gameLayout = findViewById(R.id.gameLayout);
        
        // Инициализация элементов экрана смерти
        deathScreenLayout = findViewById(R.id.deathScreenLayout);
        deadImage = findViewById(R.id.deadImage);
        deathMessageText = findViewById(R.id.deathMessageText);
        restartButton = findViewById(R.id.restartButton);

        // Кнопки действий
        Button feedButton = findViewById(R.id.feedButton);
        Button sleepButton = findViewById(R.id.sleepButton);
        Button playButton = findViewById(R.id.playButton);

        feedButton.setOnClickListener(v -> {
            creature.feed();
            showActionImage(R.drawable.eat);
            updateUI();
        });
        sleepButton.setOnClickListener(v -> {
            creature.sleep();
            showActionImage(R.drawable.sleep);
            updateUI();
        });
        playButton.setOnClickListener(v -> {
            creature.play();
            showActionImage(R.drawable.game);
            updateUI();
        });

        // Кнопка возобновления игры
        restartButton.setOnClickListener(v -> restartGame());

        startNewGame();
    }

    private void showActionImage(int imageResource) {
        // Отменить предыдущий возврат к нормальному состоянию, если он был запланирован
        if (returnToNormalRunnable != null) {
            handler.removeCallbacks(returnToNormalRunnable);
        }
        
        // Показать картинку действия
        imageView.setImageResource(imageResource);
        
        // Запланировать возврат к нормальному состоянию через 1.5 секунды
        returnToNormalRunnable = () -> {
            if (creature != null && creature.isAlive()) {
                updateCreatureImage(); // Используем метод для определения правильной картинки
            }
        };
        handler.postDelayed(returnToNormalRunnable, 1500);
    }

    private void updateCreatureImage() {
        if (creature == null) return;
        
        // Если существо мертво, показать dead.png
        if (!creature.isAlive()) {
            imageView.setImageResource(R.drawable.dead);
            return;
        }
        
        // Если голод больше 30 или скука больше 30, показать sad.png
        if (creature.getHunger() > 30 || creature.getBoredom() > 30) {
            imageView.setImageResource(R.drawable.sad);
        } else {
            // Иначе показать norm.png
            imageView.setImageResource(R.drawable.norm);
        }
    }

    private void startNewGame() {
        creature = new Creature();
        startTime = System.currentTimeMillis();
        bestTime = loadBestTime();
        gameSpeed = 2000; // сброс скорости игры
        
        // Показать игровые элементы, скрыть экран смерти
        gameLayout.setVisibility(View.VISIBLE);
        deathScreenLayout.setVisibility(View.GONE);
        
        // Включить кнопки действий
        setButtonsEnabled(true);
        
        // Установить нормальное изображение
        imageView.setImageResource(R.drawable.norm);
        
        // Обновить UI и запустить игровой цикл
        updateUI();
        handler.removeCallbacks(gameLoop); // очистить предыдущий цикл
        handler.removeCallbacks(returnToNormalRunnable); // очистить возврат к нормальному состоянию
        handler.postDelayed(gameLoop, gameSpeed);
    }

    private void showDeathScreen() {
        handler.removeCallbacks(gameLoop); // остановить игровой цикл
        handler.removeCallbacks(returnToNormalRunnable); // очистить возврат к нормальному состоянию
        saveBestTime();
        
        // Показать экран смерти, скрыть игровые элементы
        gameLayout.setVisibility(View.GONE);
        deathScreenLayout.setVisibility(View.VISIBLE);
        
        // Отключить кнопки действий
        setButtonsEnabled(false);
    }

    private void restartGame() {
        startNewGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(gameLoop);
        handler.removeCallbacks(returnToNormalRunnable);
    }

    private void updateUI() {
        if (creature == null) return;
        
        long lived = (System.currentTimeMillis() - startTime) / 1000;

        // Обновить отдельные TextView для статов
        hungerText.setText("Голод: " + creature.getHunger());
        tirednessText.setText("Усталость: " + creature.getTiredness());
        boredomText.setText("Скука: " + creature.getBoredom());
        happinessText.setText("Счастье: " + creature.getHappiness());

        // Создать текст с жирным "Рекорд"
        String timeString = "Время жизни: " + lived + " c\n";
        String recordString = "Рекорд: " + bestTime + " c";
        
        SpannableString spannable = new SpannableString(timeString + recordString);
        int recordStart = timeString.length();
        int recordEnd = recordStart + 7; // "Рекорд: " - 7 символов
        spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), recordStart, recordEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        timeText.setText(spannable);

        // Обновить изображение существа
        // Если есть активное действие (есть запланированный возврат), не менять картинку
        // Иначе обновить в зависимости от состояния
        if (returnToNormalRunnable == null || 
            !handler.hasCallbacks(returnToNormalRunnable)) {
            // Нет активного действия - обновить картинку в зависимости от состояния
            updateCreatureImage();
        }
    }

    private void saveBestTime() {
        long timeLived = (System.currentTimeMillis() - startTime) / 1000;
        var prefs = getSharedPreferences("game", MODE_PRIVATE);
        long best = prefs.getLong("best_time", 0);

        if (timeLived > best) {
            prefs.edit().putLong("best_time", timeLived).apply();
            bestTime = timeLived;
        }
    }

    private long loadBestTime() {
        var prefs = getSharedPreferences("game", MODE_PRIVATE);
        return prefs.getLong("best_time", 0);
    }

    private void setButtonsEnabled(boolean enabled) {
        findViewById(R.id.feedButton).setEnabled(enabled);
        findViewById(R.id.sleepButton).setEnabled(enabled);
        findViewById(R.id.playButton).setEnabled(enabled);
    }
}