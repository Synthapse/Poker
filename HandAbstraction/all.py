import itertools

# Define the suits and ranks
suits = ['c', 'd', 'h', 's']
ranks = ['2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A']

# Generate all 4-card combinations
deck = [r + s for r in ranks for s in suits]
combinations = itertools.combinations(deck, 4)

# Function to normalize suits (simplified placeholder)
def normalize_hand(hand):
    return ''.join(sorted(hand))

# Apply normalization
normalized_hands = [normalize_hand(comb) for comb in combinations]

# Sort and assign IDs
sorted_hands = sorted(set(normalized_hands))
hand_id_mapping = {i: sorted_hands[i] for i in range(len(sorted_hands))}

# Save to file
with open("hand_id_mapping.txt", "w") as f:
    for hand_id, hand in hand_id_mapping.items():
        f.write(f"ID: {hand_id}, Hand: {hand}\n")

print("Hand ID mapping saved to 'hand_id_mapping.txt'")
