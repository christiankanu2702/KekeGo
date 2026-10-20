/**
 * Turn-by-Turn Voice Navigation Helper for KekeGo iOS
 * Provides spoken corridor audio guidance for commercial tricycle drivers
 */

export class KekeVoiceNavigationHelper {
  private static isVoiceEnabled: boolean = true;

  static setVoiceEnabled(enabled: boolean) {
    this.isVoiceEnabled = enabled;
  }

  static getVoiceEnabled(): boolean {
    return this.isVoiceEnabled;
  }

  static speak(text: string) {
    if (!this.isVoiceEnabled) return;
    console.log(`[KekeGo iOS Voice Navigation] Spoken Cue: "${text}"`);
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
      try {
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.rate = 0.95;
        window.speechSynthesis.speak(utterance);
      } catch (_: any) {}
    }
  }

  static announceTripAccepted(pickupMarket: string) {
    this.speak(`Trip accepted. Navigate to ${pickupMarket} for passenger pickup.`);
  }

  static announceArrivalAtPickup() {
    this.speak("You have arrived at the pickup location. Notify passenger.");
  }

  static announceTripStarted(destinationMarket: string) {
    this.speak(`Trip started. Proceed along the transit corridor towards ${destinationMarket}.`);
  }

  static announceApproachingDestination(destinationMarket: string) {
    this.speak(`Approaching ${destinationMarket}. Prepare to safely alight passenger.`);
  }

  static announceTripCompleted(fareNaira: number) {
    this.speak(`Trip completed. Agreed fare of ${fareNaira} Naira collected.`);
  }
}
