#!/usr/bin/env python3
"""
Simulador SMPP Simple
listening in localhost:2775
accept smpp requests
"""

import socket
import struct
import threading
import sys

# Comandos SMPP
GENERIC_NACK = 0x80000000
BIND_RECEIVER = 0x00000001
BIND_RECEIVER_RESP = 0x80000001
BIND_TRANSMITTER = 0x00000002
BIND_TRANSMITTER_RESP = 0x80000002
BIND_TRANSCEIVER = 0x00000009
BIND_TRANSCEIVER_RESP = 0x80000009
SUBMIT_SM = 0x00000004
SUBMIT_SM_RESP = 0x80000004
UNBIND = 0x00000006
UNBIND_RESP = 0x80000006
ENQUIRE_LINK = 0x00000015
ENQUIRE_LINK_RESP = 0x80000015

# Estados
ESME_ROK = 0x00000000

class SMPPSimulator:
    def __init__(self, host='0.0.0.0', port=2775):
        self.host = host
        self.port = port
        self.message_id_counter = 1
        
    def create_pdu(self, command_id, status, sequence, body=b''):
        """Crea un PDU SMPP"""
        length = 16 + len(body)
        header = struct.pack('>IIII', length, command_id, status, sequence)
        return header + body
        
    def parse_pdu(self, data):
        """Parsea un PDU SMPP"""
        if len(data) < 16:
            return None
            
        length, command_id, status, sequence = struct.unpack('>IIII', data[:16])
        body = data[16:length] if length > 16 else b''
        
        return {
            'length': length,
            'command_id': command_id,
            'status': status,
            'sequence': sequence,
            'body': body
        }
    
    def handle_bind(self, pdu, conn):
        """Maneja BIND (Transceiver/Transmitter/Receiver)"""
        print(f"📡 BIND Request recibido (command_id: {hex(pdu['command_id'])})")
        
        # Parsear system_id del body
        try:
            system_id = pdu['body'].split(b'\x00')[0].decode('utf-8')
            print(f"   System ID: {system_id}")
        except:
            system_id = "unknown"
        
        # Responder con BIND_RESP
        if pdu['command_id'] == BIND_TRANSCEIVER:
            resp_cmd = BIND_TRANSCEIVER_RESP
        elif pdu['command_id'] == BIND_TRANSMITTER:
            resp_cmd = BIND_TRANSMITTER_RESP
        else:
            resp_cmd = BIND_RECEIVER_RESP
            
        system_id_resp = b'SMPPSim\x00'
        response = self.create_pdu(resp_cmd, ESME_ROK, pdu['sequence'], system_id_resp)
        conn.send(response)
        print(f"✅ BIND Response enviado")
        return True
    
    def handle_submit_sm(self, pdu, conn):
        """Maneja SUBMIT_SM (envío de SMS)"""
        print(f"\n📨 SUBMIT_SM recibido!")
        
        try:
            # Parsear el mensaje
            body = pdu['body']
            parts = body.split(b'\x00')
            
            # Intentar extraer información básica
            print(f"   Tamaño del body: {len(body)} bytes")
            
            # Buscar el mensaje
            for i, part in enumerate(parts):
                if len(part) > 5:
                    try:
                        msg = part.decode('utf-8', errors='ignore')
                        if 'order' in msg.lower() or 'processed' in msg.lower():
                            print(f"   📱 Mensaje SMS: '{msg}'")
                    except:
                        pass
            
        except Exception as e:
            print(f"   ⚠️  Error parseando: {e}")
        
        # Generar message_id único
        message_id = f"MSG{self.message_id_counter:08d}".encode('utf-8') + b'\x00'
        self.message_id_counter += 1
        
        # Responder con SUBMIT_SM_RESP
        response = self.create_pdu(SUBMIT_SM_RESP, ESME_ROK, pdu['sequence'], message_id)
        conn.send(response)
        print(f"✅ SUBMIT_SM_RESP enviado (Message ID: {message_id.decode().strip()})")
        return True
    
    def handle_enquire_link(self, pdu, conn):
        """Maneja ENQUIRE_LINK (keep-alive)"""
        print(f"💓 ENQUIRE_LINK recibido")
        response = self.create_pdu(ENQUIRE_LINK_RESP, ESME_ROK, pdu['sequence'])
        conn.send(response)
        print(f"✅ ENQUIRE_LINK_RESP enviado")
        return True
    
    def handle_unbind(self, pdu, conn):
        """Maneja UNBIND"""
        print(f"👋 UNBIND recibido")
        response = self.create_pdu(UNBIND_RESP, ESME_ROK, pdu['sequence'])
        conn.send(response)
        print(f"✅ UNBIND_RESP enviado")
        return False
    
    def handle_client(self, conn, addr):
        """Maneja una conexión de cliente"""
        print(f"\n🔌 Nueva conexión desde {addr}")
        
        try:
            bound = False
            
            while True:
                # Leer PDU
                header = conn.recv(16)
                if not header or len(header) < 16:
                    break
                
                length = struct.unpack('>I', header[:4])[0]
                body_length = length - 16
                
                body = b''
                if body_length > 0:
                    body = conn.recv(body_length)
                
                pdu = self.parse_pdu(header + body)
                if not pdu:
                    break
                
                # Procesar comando
                if pdu['command_id'] in [BIND_TRANSCEIVER, BIND_TRANSMITTER, BIND_RECEIVER]:
                    bound = self.handle_bind(pdu, conn)
                elif pdu['command_id'] == SUBMIT_SM:
                    self.handle_submit_sm(pdu, conn)
                elif pdu['command_id'] == ENQUIRE_LINK:
                    self.handle_enquire_link(pdu, conn)
                elif pdu['command_id'] == UNBIND:
                    if not self.handle_unbind(pdu, conn):
                        break
                else:
                    print(f"⚠️  Comando desconocido: {hex(pdu['command_id'])}")
                    
        except Exception as e:
            print(f"❌ Error: {e}")
        finally:
            conn.close()
            print(f"🔌 Conexión cerrada desde {addr}\n")
    
    def start(self):
        """Inicia el simulador"""
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        sock.bind((self.host, self.port))
        sock.listen(5)
        
        print("=" * 60)
        print("🚀 SMPP Simulator Iniciado")
        print("=" * 60)
        print(f"📡 Escuchando en {self.host}:{self.port}")
        print(f"🔑 Acepta cualquier system_id/password")
        print(f"📱 Simula envío de SMS exitoso")
        print("=" * 60)
        print("\nEsperando conexiones...\n")
        
        try:
            while True:
                conn, addr = sock.accept()
                thread = threading.Thread(target=self.handle_client, args=(conn, addr))
                thread.daemon = True
                thread.start()
        except KeyboardInterrupt:
            print("\n\n👋 Deteniendo simulador...")
            sock.close()

if __name__ == '__main__':
    port = 2775
    if len(sys.argv) > 1:
        port = int(sys.argv[1])
    
    simulator = SMPPSimulator(port=port)
    simulator.start()
