import os
from PIL import Image
import matplotlib.pyplot as plt

caption_file = r"D:\archive\captions.txt"
image_folder = r"D:\archive\Images"
separator = ','

captions = {}
with open(caption_file, "r", encoding="utf-8") as f:
    header = next(f)  # skip header
    for line in f:
        line = line.strip()
        if not line:
            continue
        if separator in line:
            img_name, caption = line.split(separator, 1)
            captions[img_name.strip()] = caption.strip()
        else:
            print(f"Skipping line (no separator '{separator}'): {line}")

print(f"Total captions loaded: {len(captions)}")

first_image = list(captions.keys())[0]
caption = captions[first_image]
image_path = os.path.join(image_folder, first_image)

if not os.path.exists(image_path):
    print(f"Image file not found: {image_path}")
    exit(1)

img = Image.open(image_path)
plt.imshow(img)
plt.title(caption)
plt.axis('off')
plt.show()
