package lcs.android.game;

/** Which activity the player's performing at the moment. Performs some minor mods to a number of
 * functions: for instance, fighting causes blood to spread about if we're on site, and bullets
 * don't hit the legs in a car chase. */
public enum GameMode {
  BASE,
  CHASECAR,
  CHASEFOOT,
  SITE,
  TITLE;
}