namespace VoidDynasty.RogueTrader {
    public static class DeliveryAvailability {
        public static bool CanRead(bool engineReady,bool loaded,bool samePlayer,bool probeBusy) {
            return engineReady && loaded && samePlayer && !probeBusy;
        }
        public static bool CanMutate(bool readable,string mode,bool combat,bool failed) {
            return readable && mode=="Default" && !combat && !failed;
        }
    }
}
