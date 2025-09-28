#!/usr/bin/env python3
"""
Script para criar imagens de teste sintéticas para o sistema distribuído.
Cria imagens coloridas simulando rostos e logos de times.
"""

from PIL import Image, ImageDraw, ImageFont
import random
import os

def criar_imagem_face(nome, largura=200, altura=200, feliz=True):
    """Cria uma imagem sintética simulando uma face"""
    # Cor de fundo baseada no sentimento
    if feliz:
        # Cores quentes para feliz
        bg_color = (random.randint(200, 255), random.randint(180, 255), random.randint(100, 200))
    else:
        # Cores frias para triste
        bg_color = (random.randint(50, 150), random.randint(50, 150), random.randint(150, 200))
    
    # Criar imagem
    img = Image.new('RGB', (largura, altura), bg_color)
    draw = ImageDraw.Draw(img)
    
    # Desenhar círculo para rosto
    face_color = (240, 200, 160)  # Cor de pele
    margin = 30
    draw.ellipse([margin, margin, largura-margin, altura-margin], fill=face_color, outline=(0,0,0), width=2)
    
    # Desenhar olhos
    eye_y = altura // 3
    eye1_x = largura // 3
    eye2_x = 2 * largura // 3
    eye_size = 15
    
    draw.ellipse([eye1_x-eye_size//2, eye_y-eye_size//2, eye1_x+eye_size//2, eye_y+eye_size//2], 
                fill=(0,0,0))
    draw.ellipse([eye2_x-eye_size//2, eye_y-eye_size//2, eye2_x+eye_size//2, eye_y+eye_size//2], 
                fill=(0,0,0))
    
    # Desenhar boca
    mouth_y = 2 * altura // 3
    mouth_x = largura // 2
    
    if feliz:
        # Sorriso
        draw.arc([mouth_x-30, mouth_y-15, mouth_x+30, mouth_y+15], 0, 180, fill=(0,0,0), width=3)
    else:
        # Boca triste
        draw.arc([mouth_x-30, mouth_y, mouth_x+30, mouth_y+30], 180, 360, fill=(0,0,0), width=3)
    
    return img

def criar_imagem_time(time, largura=200, altura=200):
    """Cria uma imagem sintética simulando logo de time"""
    cores_times = {
        'flamengo': [(255, 0, 0), (0, 0, 0)],      # Vermelho e preto
        'palmeiras': [(0, 128, 0), (255, 255, 255)], # Verde e branco
        'corinthians': [(0, 0, 0), (255, 255, 255)], # Preto e branco
        'santos': [(255, 255, 255), (0, 0, 0)],     # Branco e preto
        'saopaulo': [(255, 0, 0), (0, 0, 0), (255, 255, 255)], # Tricolor
        'gremio': [(0, 0, 255), (255, 255, 255), (0, 0, 0)],   # Azul, branco, preto
        'vasco': [(0, 0, 0), (255, 255, 255)],      # Preto e branco
        'internacional': [(255, 0, 0), (255, 255, 255)] # Vermelho e branco
    }
    
    cores = cores_times.get(time, [(128, 128, 128), (255, 255, 255)])
    
    # Criar imagem com cor principal
    img = Image.new('RGB', (largura, altura), cores[0])
    draw = ImageDraw.Draw(img)
    
    # Desenhar formas geométricas características
    if time == 'flamengo':
        # Listras vermelhas e pretas
        for i in range(0, altura, 20):
            color = cores[0] if (i // 20) % 2 == 0 else cores[1]
            draw.rectangle([0, i, largura, i+10], fill=color)
    
    elif time == 'palmeiras':
        # Círculo verde com detalhes brancos
        draw.ellipse([50, 50, largura-50, altura-50], fill=cores[0], outline=cores[1], width=5)
        draw.ellipse([80, 80, largura-80, altura-80], fill=cores[1])
    
    elif time == 'corinthians':
        # Padrão preto e branco complexo
        draw.rectangle([0, 0, largura//2, altura], fill=cores[0])
        draw.rectangle([largura//2, 0, largura, altura], fill=cores[1])
        draw.ellipse([50, 50, largura-50, altura-50], outline=(128, 128, 128), width=3)
    
    elif time == 'gremio':
        # Listras azuis
        for i in range(0, largura, 30):
            color = cores[0] if (i // 30) % 2 == 0 else cores[1]
            draw.rectangle([i, 0, i+15, altura], fill=color)
    
    elif time == 'santos':
        # Padrão branco com detalhes pretos
        draw.rectangle([0, 0, largura, altura//3], fill=cores[1])
        draw.rectangle([0, altura//3, largura, 2*altura//3], fill=cores[0])
        draw.rectangle([0, 2*altura//3, largura, altura], fill=cores[1])
    
    else:
        # Padrão genérico
        draw.rectangle([20, 20, largura-20, altura-20], fill=cores[1], outline=cores[0], width=5)
    
    return img

def main():
    """Cria todas as imagens de teste"""
    
    # Criar diretório base se não existir
    base_dir = "imagens-teste"
    os.makedirs(f"{base_dir}/faces", exist_ok=True)
    os.makedirs(f"{base_dir}/times", exist_ok=True)
    
    print("Criando imagens de teste...")
    
    # Criar imagens de faces
    faces_felizes = ['pessoa_feliz_1.jpg', 'rosto_sorrindo.jpg', 'happy_face.jpg', 'alegre_jovem.jpg']
    faces_tristes = ['pessoa_triste_1.jpg', 'rosto_chorando.jpg', 'sad_face.jpg', 'melancolia.jpg']
    
    for nome in faces_felizes:
        img = criar_imagem_face(nome, feliz=True)
        img.save(f"{base_dir}/faces/{nome}")
        print(f"  Criado: faces/{nome} (feliz)")
    
    for nome in faces_tristes:
        img = criar_imagem_face(nome, feliz=False)
        img.save(f"{base_dir}/faces/{nome}")
        print(f"  Criado: faces/{nome} (triste)")
    
    # Criar imagens de times
    times = ['flamengo', 'palmeiras', 'corinthians', 'santos', 'saopaulo', 'gremio', 'vasco', 'internacional']
    
    for time in times:
        for i in range(1, 3):  # 2 imagens por time
            nome = f"logo_{time}_{i}.jpg"
            img = criar_imagem_time(time)
            img.save(f"{base_dir}/times/{nome}")
            print(f"  Criado: times/{nome}")
    
    print(f"\n✅ Imagens criadas com sucesso!")
    print(f"📁 Faces: {len(faces_felizes + faces_tristes)} imagens em {base_dir}/faces/")
    print(f"📁 Times: {len(times) * 2} imagens em {base_dir}/times/")
    print(f"\nAgora você pode executar o sistema e ele processará essas imagens!")

if __name__ == "__main__":
    main()